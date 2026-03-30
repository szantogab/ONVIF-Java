package be.teletask.onvif.rxjava

import be.teletask.onvif.DiscoveryManager
import be.teletask.onvif.listeners.DiscoveryListener
import be.teletask.onvif.models.Device
import be.teletask.onvif.models.OnvifDevice
import be.teletask.onvif.requests.OnvifRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Callback-alapú ONVIF kérések RxJava3 `Single`/`Completable` API-ra fordítása.
 *
 * Megjegyzés: a mögöttes réteg nem biztosít lemondást, ezért a `dispose` után is előfordulhat,
 * hogy a callback később befut. Ezt lokálisan `done` flag-gel és `emitter.isDisposed` ellenőrzéssel kezeljük.
 */
fun <T : Any> awaitDeviceRequest(block: (OnvifRequest.Listener<T>) -> Unit): Single<T> = Single.create { emitter ->
	val done = AtomicBoolean(false)

	val listener = object : OnvifRequest.Listener<T> {
		override fun onSuccess(device: OnvifDevice?, data: T) {
			if (done.compareAndSet(false, true) && !emitter.isDisposed) {
				emitter.onSuccess(data)
			}
		}

		override fun onError(onvifException: OnvifRequest.OnvifException) {
			if (done.compareAndSet(false, true) && !emitter.isDisposed) {
				emitter.onError(onvifException)
			}
		}
	}

	try {
		block(listener)
	} catch (t: Throwable) {
		if (done.compareAndSet(false, true) && !emitter.isDisposed) {
			emitter.onError(t)
		}
	}
}

fun awaitDeviceRequestCompletable(block: (OnvifRequest.Listener<Void>) -> Unit): Completable = Completable.create { emitter ->
	val done = AtomicBoolean(false)

	val listener = object : OnvifRequest.Listener<Void> {
		// A Java oldalon a `Void` lehet null; Kotlinban legyen null-kompatibilis.
		override fun onSuccess(device: OnvifDevice?, data: Void?) {
			if (done.compareAndSet(false, true) && !emitter.isDisposed) {
				emitter.onComplete()
			}
		}

		override fun onError(onvifException: OnvifRequest.OnvifException) {
			if (done.compareAndSet(false, true) && !emitter.isDisposed) {
				emitter.onError(onvifException)
			}
		}
	}

	try {
		block(listener)
	} catch (t: Throwable) {
		if (done.compareAndSet(false, true) && !emitter.isDisposed) {
			emitter.onError(t)
		}
	}
}

fun awaitDeviceDiscovery(
	onDiscoveryStarted: () -> Unit = {},
	block: (DiscoveryListener) -> Unit,
): Single<List<Device>> = Single.create { emitter ->
	val done = AtomicBoolean(false)

	val listener = object : DiscoveryListener {
		override fun onDiscoveryStarted() {
			onDiscoveryStarted()
		}

		override fun onDevicesFound(devices: List<Device>) {
			if (done.compareAndSet(false, true) && !emitter.isDisposed) {
				emitter.onSuccess(devices)
			}
		}
	}

	try {
		block(listener)
	} catch (t: Throwable) {
		if (done.compareAndSet(false, true) && !emitter.isDisposed) {
			emitter.onError(t)
		}
	}
}

fun discoverDevices(configure: DiscoveryManager.() -> Unit = {}): Single<List<Device>> = Single.defer {
	val discoveryManager = DiscoveryManager()
	discoveryManager.configure()

	awaitDeviceDiscovery(block = { listener ->
		discoveryManager.discover(listener)
	})
}

