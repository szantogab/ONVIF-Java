package be.teletask.onvif.examples

import be.teletask.onvif.*
import be.teletask.onvif.models.OnvifDevice
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

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
}

/*
package be.teletask.onvif.examples

import be.teletask.onvif.getMediaProfiles
import be.teletask.onvif.getMediaSnapshot
import be.teletask.onvif.getMediaSnapshotUri
import be.teletask.onvif.getMotionDetectionConfiguration
import be.teletask.onvif.setMotionDetectionConfiguration
import be.teletask.onvif.OnvifManager
import be.teletask.onvif.models.OnvifDevice
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class MotionDetectionExampleRxTest {

    private fun requireEnvVar(name: String): String {
        val value = System.getenv(name)
        Assumptions.assumeTrue(
            !value.isNullOrBlank(),
            "Set $name to run integration tests (motion snapshot/media profile + motion detection).",
        )
        return value!!
    }

    private fun requireDeviceFromEnv(): OnvifDevice {
        val host = requireEnvVar("ONVIF_HOST")
        val user = requireEnvVar("ONVIF_USER")
        val password = requireEnvVar("ONVIF_PASSWORD")
        return OnvifDevice(host, user, password)
    }

    @Test
    @Timeout(60)
    fun `media snapshot uri is not null and snapshot bytes are not empty`() {
        val device = requireDeviceFromEnv()
        val manager = OnvifManager()

        try {
            val profiles = device.getMediaProfiles(manager).blockingGet()
            assertTrue(profiles.isNotEmpty(), "Expected at least one media profile")

            val profile = profiles.first()
            val snapshotUri = device.getMediaSnapshotUri(profile, manager).blockingGet()
            assertNotNull(snapshotUri)
            assertTrue(snapshotUri.isNotBlank(), "Expected non-blank snapshot uri")

            val snapshotBytes = device.getMediaSnapshot(snapshotUri, timeoutSeconds = 5, om = manager)
                .blockingGet()
            assertNotNull(snapshotBytes)
            assertTrue(snapshotBytes.isNotEmpty(), "Expected non-empty snapshot bytes")
        } finally {
            manager.destroy()
        }
    }

    @Test
    @Timeout(60)
    fun `motion detection configuration can be read and set without error`() {
        val device = requireDeviceFromEnv()
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
}

package be.teletask.onvif.examples

import be.teletask.onvif.getMediaProfiles
import be.teletask.onvif.getMediaSnapshot
import be.teletask.onvif.getMediaSnapshotUri
import be.teletask.onvif.getMotionDetectionConfiguration
import be.teletask.onvif.setMotionDetectionConfiguration
import be.teletask.onvif.OnvifManager
import be.teletask.onvif.models.OnvifDevice
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class MotionDetectionExampleRxTest {

    private fun requireEnvVar(name: String): String {
        val value = System.getenv(name)
        Assumptions.assumeTrue(
            !value.isNullOrBlank(),
            "Set $name to run integration tests (motion snapshot/media profile + motion detection).",
        )
        return value!!
    }

    private fun requireDeviceFromEnv(): OnvifDevice {
        val host = requireEnvVar("ONVIF_HOST")
        val user = requireEnvVar("ONVIF_USER")
        val password = requireEnvVar("ONVIF_PASSWORD")
        return OnvifDevice(host, user, password)
    }

    @Test
    @Timeout(60)
    fun `media snapshot uri is not null and snapshot bytes are not empty`() {
        val device = requireDeviceFromEnv()
        val manager = OnvifManager()

        try {
            val profiles = device.getMediaProfiles(manager).blockingGet()
            assertTrue(profiles.isNotEmpty(), "Expected at least one media profile")

            val profile = profiles.first()
            val snapshotUri = device.getMediaSnapshotUri(profile, manager).blockingGet()
            assertNotNull(snapshotUri)
            assertTrue(snapshotUri.isNotBlank(), "Expected non-blank snapshot uri")

            val snapshotBytes = device.getMediaSnapshot(snapshotUri, timeoutSeconds = 5, om = manager)
                .blockingGet()
            assertNotNull(snapshotBytes)
            assertTrue(snapshotBytes.isNotEmpty(), "Expected non-empty snapshot bytes")
        } finally {
            manager.destroy()
        }
    }

    @Test
    @Timeout(60)
    fun `motion detection configuration can be read and set without error`() {
        val device = requireDeviceFromEnv()
        val manager = OnvifManager()

        try {
            val current = device.getMotionDetectionConfiguration(manager).blockingGet()
            assertNotNull(current)

            // Minimális (és biztonságos) integráció: ugyanazt a konfigurációt visszaküldjük,
            // hogy a `set...` ut is hibamentesen fusson le a valós eszközzel.
            device.setMotionDetectionConfiguration(current, manager).blockingAwait()
            assertTrue(true)
        } finally {
            manager.destroy()
        }
    }
}

package be.teletask.onvif.examples

import be.teletask.onvif.getMotionDetectionConfiguration
import be.teletask.onvif.getMediaProfiles
import be.teletask.onvif.getMediaSnapshot
import be.teletask.onvif.getMediaSnapshotUri
import be.teletask.onvif.setMotionDetectionConfiguration
import be.teletask.onvif.OnvifManager
import be.teletask.onvif.models.OnvifDevice
import be.teletask.onvif.models.OnvifMediaProfile
import be.teletask.onvif.models.OnvifMotionDetection
import be.teletask.onvif.requests.OnvifRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MotionDetectionExampleRxTest {
	val device = OnvifDevice("192.168.0.119", "smartive", "smartive1")

	private fun unwrapOnvifException(t: Throwable): OnvifRequest.OnvifException {
		var current: Throwable? = t
		while (current != null) {
			if (current is OnvifRequest.OnvifException) return current
			current = current.cause
		}
		throw AssertionError("Nincs OnvifException a cause-láncban. T: $t")
	}

	private class FakeOnvifManager(
		private val getResult: Result<OnvifMotionDetection>,
		private val setResult: Result<Unit> = Result.success(Unit),
		private val mediaProfilesResult: Result<List<OnvifMediaProfile>> = Result.success(emptyList()),
		private val mediaSnapshotUriResult: Result<String> = Result.success("snapshot://example/not-null"),
		private val mediaSnapshotResult: Result<ByteArray> = Result.success(byteArrayOf(1, 2, 3)),
	) : OnvifManager() {
		var lastSetValue: OnvifMotionDetection? = null
		var lastSnapshotUri: String? = null

		override fun getMotionDetectionConfiguration(device: OnvifDevice, listener: OnvifRequest.Listener<OnvifMotionDetection>) {
			getResult.fold(
					onSuccess = { listener.onSuccess(device, it) },
					onFailure = { listener.onError(it as OnvifRequest.OnvifException) },
			)
		}

		override fun setMotionDetectionConfiguration(device: OnvifDevice, motionDetection: OnvifMotionDetection, listener: OnvifRequest.Listener<Void>) {
			lastSetValue = motionDetection
			setResult.fold(
					onSuccess = { listener.onSuccess(device, null) },
					onFailure = { listener.onError(it as OnvifRequest.OnvifException) },
			)
		}

		override fun getMediaProfiles(
			device: OnvifDevice,
			listener: OnvifRequest.Listener<List<OnvifMediaProfile>>,
		) {
			mediaProfilesResult.fold(
				onSuccess = { listener.onSuccess(device, it) },
				onFailure = { listener.onError(it as OnvifRequest.OnvifException) },
			)
		}

		override fun getMediaSnapshotURI(
			device: OnvifDevice,
			profile: OnvifMediaProfile,
			listener: OnvifRequest.Listener<String>,
		) {
			mediaSnapshotUriResult.fold(
				onSuccess = { listener.onSuccess(device, it) },
				onFailure = { listener.onError(it as OnvifRequest.OnvifException) },
			)
		}

		override fun getMediaSnapshot(
			device: OnvifDevice,
			snapshotUri: String,
			timeoutSeconds: Int,
			listener: OnvifRequest.Listener<ByteArray>,
		) {
			lastSnapshotUri = snapshotUri
			mediaSnapshotResult.fold(
				onSuccess = { listener.onSuccess(device, it) },
				onFailure = { listener.onError(it as OnvifRequest.OnvifException) },
			)
		}
	}

	@Test
	fun `getMotionDetectionConfiguration Single emits expected token`() {
		val expected = OnvifMotionDetection().apply {
			setToken("token-123")
			setName("name")
			setEnabled(true)
			setSourceToken("source")
		}
		val manager = FakeOnvifManager(Result.success(expected))

		val actual = device.getMotionDetectionConfiguration(manager).blockingGet()

		assertEquals("token-123", actual.token)
	}

	@Test
	fun `getMotionDetectionConfiguration Single propagates OnvifException`() {
		val exception = OnvifRequest.OnvifException(device, 500, "get failed")
		val manager = FakeOnvifManager(Result.failure(exception))

		val thrown = assertThrows(Throwable::class.java) {
			device.getMotionDetectionConfiguration(manager).blockingGet()
		}

		val onvif = unwrapOnvifException(thrown)
		assertEquals("get failed", onvif.message)
	}

	@Test
	fun `setMotionDetectionConfiguration Completable completes and passes value`() {
		val expected = OnvifMotionDetection().apply {
			setToken("set-token")
			setName("set-name")
			setEnabled(true)
			setSourceToken("set-source")
		}
		val manager = FakeOnvifManager(Result.success(OnvifMotionDetection()))

		device.setMotionDetectionConfiguration(expected, manager).blockingAwait()

		assertNotNull(manager.lastSetValue)
		assertEquals("set-token", manager.lastSetValue!!.token)
	}

	@Test
	fun `setMotionDetectionConfiguration Completable propagates OnvifException`() {
		val expected = OnvifMotionDetection().apply {
			setToken("set-token")
			setName("set-name")
			setEnabled(true)
			setSourceToken("set-source")
		}

		val exception = OnvifRequest.OnvifException(device, 503, "set failed")
		val manager = FakeOnvifManager(
				getResult = Result.success(OnvifMotionDetection()),
				setResult = Result.failure(exception),
		)

		val thrown = assertThrows(Throwable::class.java) {
			device.setMotionDetectionConfiguration(expected, manager).blockingAwait()
		}

		val onvif = unwrapOnvifException(thrown)
		assertEquals("set failed", onvif.message)
	}

	@Test
	fun `media snapshot uri is not null and snapshot bytes are returned`() {
		val profile = OnvifMediaProfile("profile-name", "profile-token")
		val expectedSnapshotUri = "http://example/snapshot/profile-token"
		val expectedSnapshotBytes = byteArrayOf(9, 8, 7, 6)

		val manager = FakeOnvifManager(
			getResult = Result.success(OnvifMotionDetection()),
			mediaProfilesResult = Result.success(listOf(profile)),
			mediaSnapshotUriResult = Result.success(expectedSnapshotUri),
			mediaSnapshotResult = Result.success(expectedSnapshotBytes),
		)

		val profiles = device.getMediaProfiles(manager).blockingGet()
		assertTrue(profiles.isNotEmpty())

		val snapshotUri = device.getMediaSnapshotUri(profiles.first(), manager).blockingGet()
		assertNotNull(snapshotUri)
		assertTrue(snapshotUri.isNotBlank())
		assertEquals(expectedSnapshotUri, snapshotUri)

		val snapshotBytes = device.getMediaSnapshot(snapshotUri, om = manager).blockingGet()
		assertTrue(snapshotBytes.isNotEmpty())
		assertEquals(expectedSnapshotBytes.size, snapshotBytes.size)
}
}
*/