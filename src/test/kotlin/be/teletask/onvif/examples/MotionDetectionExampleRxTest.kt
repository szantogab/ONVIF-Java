package be.teletask.onvif.examples

import be.teletask.onvif.*
import be.teletask.onvif.models.OnvifDevice
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class MotionDetectionExampleRxTest {

	private fun requireDeviceFromEnvOrExample(): OnvifDevice {
		// Ha CI-ben vagy máshol nem akarod, állítsd:
		// ONVIF_TEST_DISABLE_INTEGRATION=true
		val disableIntegration = System.getenv("ONVIF_TEST_DISABLE_INTEGRATION") == "true"
		Assumptions.assumeTrue(!disableIntegration, "Integration tests disabled via ONVIF_TEST_DISABLE_INTEGRATION")

		val host = System.getenv("ONVIF_HOST") ?: "192.168.0.119"
		val user = System.getenv("ONVIF_USER") ?: "smartive"
		val password = System.getenv("ONVIF_PASSWORD") ?: "smartive1"

		Assumptions.assumeTrue(host.isNotBlank(), "Set ONVIF_HOST to run integration tests")
		Assumptions.assumeTrue(user.isNotBlank(), "Set ONVIF_USER to run integration tests")
		Assumptions.assumeTrue(password.isNotBlank(), "Set ONVIF_PASSWORD to run integration tests")

		return OnvifDevice(host, user, password)
	}

	@Test
	@Timeout(60)
	fun `media snapshot uri is not null and snapshot bytes are not empty`() {
		val device = requireDeviceFromEnvOrExample()
		val manager = OnvifManager()

		try {
			val profiles = device.getMediaProfiles(manager).blockingGet()
			assertTrue(profiles.isNotEmpty(), "Expected at least one media profile")

			val profile = profiles.first()
			val snapshotUri = device.getMediaSnapshotUri(profile, manager).blockingGet()
			assertNotNull(snapshotUri)
			assertTrue(snapshotUri.isNotBlank(), "Expected non-blank snapshot uri")

			val snapshotBytes = device.getMediaSnapshot(snapshotUri, timeoutSeconds = 5, om = manager).blockingGet()
			assertNotNull(snapshotBytes)
			assertTrue(snapshotBytes.isNotEmpty(), "Expected non-empty snapshot bytes")
		} finally {
			manager.destroy()
		}
	}

	@Test
	@Timeout(60)
	fun `motion detection configuration can be read and set without error`() {
		val device = requireDeviceFromEnvOrExample()
		val manager = OnvifManager()

		try {
			val current = device.getMotionDetectionConfiguration(manager).blockingGet()
			assertNotNull(current)

			// Minimális (és biztonságos) integráció: ugyanazt a konfigurációt visszaküldjük,
			// hogy a `set...` ut is hibamentesen fusson le a valós eszközzel.
			device.setMotionDetectionConfiguration(current, manager).blockingAwait()
		} finally {
			manager.destroy()
		}
	}

	@Test
	fun `pull point subscription flowable works with real device`() {
		val device = requireDeviceFromEnvOrExample()
		val manager = OnvifManager()

		val eventCount = AtomicInteger(0)
		val errorRef = AtomicReference<Throwable?>(null)

		val disposable = device.createPullPointSubscription(
			eventFilters = null,
			initialTerminationTimeSeconds = 60,
			messageLimit = 20,
			pullTimeoutSeconds = 10,
			// Integrációs teszt: gyors külső retry (régi minta: retryWhen delay)
			retryDelaySeconds = 0,
			pullRepeatDelayMillis = 200,
			om = manager
		).subscribe(
			{ event ->
				println("Received event $event.")
				eventCount.incrementAndGet()
			},
			{ err ->
				println("Received error: $err.")
				errorRef.set(err)
			}
		)

		try {
			// Adj időt a PullMessages ciklusnak (és a lehetséges retry-nak) pár kör lefutására.
			val deadlineMs = System.nanoTime() + TimeUnit.SECONDS.toNanos(15)
			while (System.nanoTime() < deadlineMs && errorRef.get() == null) {
				Thread.sleep(100)
			}

			assertTrue(errorRef.get() == null, "Expected no subscription error, got: ${errorRef.get()}")

			val beforeDispose = eventCount.get()

			// Dispose után ne jöjjön több event (a callback később is befuthat, de Rx oldalon el kell nyelődnie).
			disposable.dispose()
			Thread.sleep(1_500)

			assertTrue(
				eventCount.get() == beforeDispose,
				"Expected no new events after dispose (before=$beforeDispose, after=${eventCount.get()})"
			)
		} finally {
			// A dispose már megtörtént, de biztonság kedvéért idempotens.
			disposable.dispose()
			manager.destroy()
		}
	}
}
