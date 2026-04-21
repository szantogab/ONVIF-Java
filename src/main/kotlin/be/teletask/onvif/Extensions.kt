package be.teletask.onvif

import be.teletask.onvif.models.*
import be.teletask.onvif.requests.OnvifRequest
import be.teletask.onvif.rxjava.awaitDeviceRequest
import be.teletask.onvif.rxjava.awaitDeviceRequestCompletable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

val defaultOnvifManager by lazy { OnvifManager() }

fun OnvifDevice.getInformation(om: OnvifManager = defaultOnvifManager): Single<OnvifDeviceInformation> = awaitDeviceRequest { om.getDeviceInformation(this, it) }


fun OnvifDevice.getMediaProfiles(om: OnvifManager = defaultOnvifManager): Single<List<OnvifMediaProfile>> = awaitDeviceRequest { om.getMediaProfiles(this, it) }

fun OnvifDevice.getMediaStreamUri(profile: OnvifMediaProfile, om: OnvifManager = defaultOnvifManager): Single<String> = awaitDeviceRequest { om.getMediaStreamURI(this, profile, it) }

fun OnvifDevice.getMediaSnapshotUri(profile: OnvifMediaProfile, om: OnvifManager = defaultOnvifManager): Single<String> = awaitDeviceRequest { om.getMediaSnapshotURI(this, profile, it) }

fun OnvifDevice.getMediaSnapshot(snapshotUri: String, timeoutSeconds: Int = 3, om: OnvifManager = defaultOnvifManager): Single<ByteArray> = awaitDeviceRequest { om.getMediaSnapshot(this, snapshotUri, timeoutSeconds, it) }

fun OnvifDevice.ptzContinuousMove(profileToken: String, velocityX: Double, velocityY: Double, velocityZ: Double?, timeout: Int?, om: OnvifManager = defaultOnvifManager): Completable = awaitDeviceRequestCompletable { om.ptzContinuousMove(this, profileToken, velocityX, velocityY, velocityZ, timeout, it) }

fun OnvifDevice.ptzRelativeMove(profileToken: String, translationX: Double?, translationY: Double?, zoom: Double?, om: OnvifManager = defaultOnvifManager): Completable = awaitDeviceRequestCompletable { om.ptzRelativeMove(this, profileToken, translationX, translationY, zoom, it) }

fun OnvifDevice.ptzAbsoluteMove(profileToken: String, positionX: Double?, positionY: Double?, zoom: Double?, om: OnvifManager = defaultOnvifManager): Completable = awaitDeviceRequestCompletable { om.ptzAbsoluteMove(this, profileToken, positionX, positionY, zoom, it) }

fun OnvifDevice.ptzStop(profileToken: String, panTilt: Boolean = true, zoom: Boolean = true, om: OnvifManager = defaultOnvifManager): Completable = awaitDeviceRequestCompletable { om.ptzStop(this, profileToken, panTilt, zoom, it) }

// Motion detection extensions

fun OnvifDevice.getMotionDetectionConfiguration(om: OnvifManager = defaultOnvifManager): Single<OnvifMotionDetection> = awaitDeviceRequest { om.getMotionDetectionConfiguration(this, it) }

fun OnvifDevice.setMotionDetectionConfiguration(motionDetection: OnvifMotionDetection, om: OnvifManager = defaultOnvifManager): Completable = awaitDeviceRequestCompletable { om.setMotionDetectionConfiguration(this, motionDetection, it) }

fun OnvifDevice.getAnalyticsEngines(om: OnvifManager = defaultOnvifManager): Single<List<String>> = awaitDeviceRequest { om.getAnalyticsEngines(this, it) }

fun OnvifDevice.createPullPointSubscriptionReference(
	eventFilters: Array<String>? = null,
	initialTerminationTimeSeconds: Int = 60,
	/** HTTP timeout a `CreatePullPointSubscription` kérésre (alapból nem 10s, mert sok eszköz lassabb / hibás választ ad rövid határidőn). */
	om: OnvifManager = defaultOnvifManager,
): Single<String> = awaitDeviceRequest { om.createPullPointSubscription(this, eventFilters, initialTerminationTimeSeconds, it) }

/**
 * ONVIF PullPoint subscription + polling, a korábbi app-kód szerinti operátor-struktúrával:
 *
 * ```
 * motionSubscribe().switchMap { subscriptionAddress ->
 *     pullMessages(...).repeatWhen { completed -> completed.delay(10, MILLISECONDS) }
 * }.retryWhen { it.delay(5, SECONDS) }
 * ```
 *
 * - `motionSubscribe` megfelelője: egy `defer` + `concatWith(never())`, hogy egy `onNext(subscriptionAddress)`
 *   után ne `complete`-eljen (mint a régi `Flowable.create` + `LATEST`).
 * - Bármilyen hiba után a külső `retryWhen` újra feliratkozik → új subscription + új pull lánc.
 * - A `switchMap` belső ág `doFinally`-je leiratkozik az adott `subscriptionAddress`-ről.
 *
 * A `distinctUntilChanged().replayingShare()` maradjon a hívó oldalon (pl. mozgás-Boolean streamhez), ha kell.
 */
fun OnvifDevice.createPullPointSubscription(
	eventFilters: Array<String>? = null,
	initialTerminationTimeSeconds: Int = 60,
	messageLimit: Int = 20,
	pullTimeoutSeconds: Int = 60,
	/** Delay between full resubscribe attempts after terminal errors. */
	retryDelaySeconds: Long = 5,
	/** Delay between consecutive pullMessages cycles. */
	pullRepeatDelayMillis: Long = 1000,
	om: OnvifManager = defaultOnvifManager,
	timerScheduler: io.reactivex.rxjava3.core.Scheduler = Schedulers.io(),
): Flowable<OnvifMotionEvent> {
	val device = this

	return Flowable.defer {
		createPullPointSubscriptionReference(
				eventFilters = eventFilters,
				initialTerminationTimeSeconds = initialTerminationTimeSeconds,
				om = om,
		).toFlowable()
	}.switchMap { subscriptionAddress ->
		pullMessages(
				subscriptionReference = subscriptionAddress,
				messageLimit = messageLimit,
				timeoutSeconds = pullTimeoutSeconds,
				om = om,
		)
			.toFlowable()
			.concatMapIterable { it } // lower churn than flatMapIterable for this ordered stream
			.repeatWhen { completed ->
				completed.delay(pullRepeatDelayMillis, TimeUnit.MILLISECONDS, timerScheduler)
			}
			.doFinally {
				om.unsubscribe(device, subscriptionAddress, null)
			}
	}.retryWhen { errors ->
		errors.delay(retryDelaySeconds, TimeUnit.SECONDS, timerScheduler)
	}
}

fun OnvifDevice.pullMessages(subscriptionReference: String, messageLimit: Int = 20, timeoutSeconds: Int = 60, om: OnvifManager = defaultOnvifManager): Single<List<OnvifMotionEvent>> = awaitDeviceRequest { om.pullMessages(this, subscriptionReference, messageLimit, timeoutSeconds, it) }

// Events – GetEventProperties

fun OnvifDevice.getEventProperties(om: OnvifManager = defaultOnvifManager): Single<OnvifEventProperties> = awaitDeviceRequest { om.getEventProperties(this, it) }

// Event Broker extensions

fun OnvifDevice.addEventBroker(brokerAddress: String, om: OnvifManager = defaultOnvifManager): Completable = awaitDeviceRequestCompletable { om.addEventBroker(this, brokerAddress, it) }

fun OnvifDevice.deleteEventBroker(brokerToken: String, om: OnvifManager = defaultOnvifManager): Completable = awaitDeviceRequestCompletable { om.deleteEventBroker(this, brokerToken, it) }

fun OnvifDevice.getEventBrokers(om: OnvifManager = defaultOnvifManager): Single<List<String>> = awaitDeviceRequest { om.getEventBrokers(this, it) }

// Unsubscribe extension

fun OnvifDevice.unsubscribe(subscriptionReference: String, om: OnvifManager = defaultOnvifManager): Completable = awaitDeviceRequestCompletable { om.unsubscribe(this, subscriptionReference, it) }
