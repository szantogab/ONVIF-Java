package be.teletask.onvif

import be.teletask.onvif.rxjava.awaitDeviceRequest
import be.teletask.onvif.rxjava.awaitDeviceRequestCompletable
import be.teletask.onvif.models.OnvifDevice
import be.teletask.onvif.models.OnvifDeviceInformation
import be.teletask.onvif.models.OnvifMediaProfile
import be.teletask.onvif.models.OnvifMotionDetection
import be.teletask.onvif.models.OnvifMotionEvent
import be.teletask.onvif.models.OnvifEventProperties
import be.teletask.onvif.requests.OnvifRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

val defaultOnvifManager by lazy { OnvifManager() }

fun OnvifDevice.getInformation(om: OnvifManager = defaultOnvifManager): Single<OnvifDeviceInformation> =
    awaitDeviceRequest { om.getDeviceInformation(this, it) }


fun OnvifDevice.getMediaProfiles(om: OnvifManager = defaultOnvifManager): Single<List<OnvifMediaProfile>> =
    awaitDeviceRequest { om.getMediaProfiles(this, it) }

fun OnvifDevice.getMediaStreamUri(
    profile: OnvifMediaProfile,
    om: OnvifManager = defaultOnvifManager
): Single<String> =
    awaitDeviceRequest { om.getMediaStreamURI(this, profile, it) }

fun OnvifDevice.getMediaSnapshotUri(
    profile: OnvifMediaProfile,
    om: OnvifManager = defaultOnvifManager
): Single<String> =
    awaitDeviceRequest { om.getMediaSnapshotURI(this, profile, it) }

fun OnvifDevice.getMediaSnapshot(
    snapshotUri: String,
    timeoutSeconds: Int = 3,
    om: OnvifManager = defaultOnvifManager
): Single<ByteArray> =
    awaitDeviceRequest { om.getMediaSnapshot(this, snapshotUri, timeoutSeconds, it) }

fun OnvifDevice.ptzContinuousMove(
    profileToken: String,
    velocityX: Double,
    velocityY: Double,
    velocityZ: Double?,
    timeout: Int?,
    om: OnvifManager = defaultOnvifManager
): Completable =
    awaitDeviceRequestCompletable { om.ptzContinuousMove(this, profileToken, velocityX, velocityY, velocityZ, timeout, it) }

fun OnvifDevice.ptzRelativeMove(
    profileToken: String,
    translationX: Double?,
    translationY: Double?,
    zoom: Double?,
    om: OnvifManager = defaultOnvifManager
): Completable =
    awaitDeviceRequestCompletable { om.ptzRelativeMove(this, profileToken, translationX, translationY, zoom, it) }

fun OnvifDevice.ptzAbsoluteMove(
    profileToken: String,
    positionX: Double?,
    positionY: Double?,
    zoom: Double?,
    om: OnvifManager = defaultOnvifManager
): Completable =
    awaitDeviceRequestCompletable { om.ptzAbsoluteMove(this, profileToken, positionX, positionY, zoom, it) }

fun OnvifDevice.ptzStop(
    profileToken: String,
    panTilt: Boolean = true,
    zoom: Boolean = true,
    om: OnvifManager = defaultOnvifManager
): Completable =
    awaitDeviceRequestCompletable { om.ptzStop(this, profileToken, panTilt, zoom, it) }

// Motion detection extensions

fun OnvifDevice.getMotionDetectionConfiguration(
    om: OnvifManager = defaultOnvifManager
): Single<OnvifMotionDetection> =
    awaitDeviceRequest { om.getMotionDetectionConfiguration(this, it) }

fun OnvifDevice.setMotionDetectionConfiguration(
    motionDetection: OnvifMotionDetection,
    om: OnvifManager = defaultOnvifManager
): Completable =
    awaitDeviceRequestCompletable { om.setMotionDetectionConfiguration(this, motionDetection, it) }

fun OnvifDevice.getAnalyticsEngines(
    om: OnvifManager = defaultOnvifManager
): Single<List<String>> =
    awaitDeviceRequest { om.getAnalyticsEngines(this, it) }

fun OnvifDevice.createPullPointSubscriptionReference(
    eventFilters: Array<String>? = null,
    initialTerminationTimeSeconds: Int = 60,
    om: OnvifManager = defaultOnvifManager
): Single<String> =
    awaitDeviceRequest { om.createPullPointSubscription(this, eventFilters, initialTerminationTimeSeconds, it) }

/**
 * ONVIF PullPoint subscription + polling Flowable.
 *
 * - subscribe esetén: `CreatePullPointSubscription`
 * - majd: `PullMessages` ciklus (polling), és az események emitálása
 * - timeout / kapcsolat megszakadás esetén: újracsatlakozás (retry)
 * - a Flowable leiratkozásakor (`dispose`/`unsubscribe`): ONVIF `Unsubscribe`
 */
fun OnvifDevice.createPullPointSubscription(
    eventFilters: Array<String>? = null,
    initialTerminationTimeSeconds: Int = 60,
    messageLimit: Int = 20,
    pullTimeoutSeconds: Int = 60,
    retryDelayMillis: Long = 1_000,
    maxRetryAttempts: Long = Long.MAX_VALUE,
    om: OnvifManager = defaultOnvifManager
): Flowable<OnvifMotionEvent> = Flowable.defer {
    val device = this
    val currentSubscriptionReference = AtomicReference<String?>(null)

    fun Throwable.isRetryablePullFailure(): Boolean {
        if (this !is OnvifRequest.OnvifException) return false

        val msg = generateSequence<Throwable>(this) { it.cause }
            .mapNotNull { it.message }
            .joinToString(" ")
            .lowercase()

        val looksLikeTimeout =
            msg.contains("timed out") ||
                msg.contains("timeout") ||
                msg.contains("timedout")

        val looksLikeConnectionFailure =
            msg.contains("connection reset") ||
                msg.contains("connection refused") ||
                msg.contains("broken pipe") ||
                msg.contains("eof") ||
                msg.contains("connection aborted") ||
                (msg.contains("socket") && msg.contains("closed"))

        return looksLikeTimeout || looksLikeConnectionFailure
    }

    Flowable.defer {
        createPullPointSubscriptionReference(
            eventFilters = eventFilters,
            initialTerminationTimeSeconds = initialTerminationTimeSeconds,
            om = om
        )
            .toFlowable()
            .flatMap { subscriptionReference ->
                currentSubscriptionReference.set(subscriptionReference)

                pullMessages(
                    subscriptionReference = subscriptionReference,
                    messageLimit = messageLimit,
                    timeoutSeconds = pullTimeoutSeconds,
                    om = om
                )
                    .toFlowable()
                    .flatMapIterable { it }
                    .repeat()
            }
    }
        .retryWhen { errors ->
            var attempts = 0L
            errors.flatMap { err ->
                attempts++
                if (attempts > maxRetryAttempts || !err.isRetryablePullFailure()) {
                    return@flatMap Flowable.error(err)
                }
                Flowable.timer(retryDelayMillis, TimeUnit.MILLISECONDS)
            }
        }
        .doFinally {
            currentSubscriptionReference.getAndSet(null)?.let { subscriptionReference ->
                // Legjobb erőfeszítés: erőforrás felszabadítása leiratkozáskor.
                om.unsubscribe(device, subscriptionReference, null)
            }
        }
}

fun OnvifDevice.pullMessages(
    subscriptionReference: String,
    messageLimit: Int = 20,
    timeoutSeconds: Int = 60,
    om: OnvifManager = defaultOnvifManager
): Single<List<OnvifMotionEvent>> =
    awaitDeviceRequest { om.pullMessages(this, subscriptionReference, messageLimit, timeoutSeconds, it) }

// Events – GetEventProperties

fun OnvifDevice.getEventProperties(
    om: OnvifManager = defaultOnvifManager
): Single<OnvifEventProperties> =
    awaitDeviceRequest { om.getEventProperties(this, it) }

// Event Broker extensions

fun OnvifDevice.addEventBroker(
    brokerAddress: String,
    om: OnvifManager = defaultOnvifManager
): Completable =
    awaitDeviceRequestCompletable { om.addEventBroker(this, brokerAddress, it) }

fun OnvifDevice.deleteEventBroker(
    brokerToken: String,
    om: OnvifManager = defaultOnvifManager
): Completable =
    awaitDeviceRequestCompletable { om.deleteEventBroker(this, brokerToken, it) }

fun OnvifDevice.getEventBrokers(
    om: OnvifManager = defaultOnvifManager
): Single<List<String>> =
    awaitDeviceRequest { om.getEventBrokers(this, it) }

// Unsubscribe extension

fun OnvifDevice.unsubscribe(
    subscriptionReference: String,
    om: OnvifManager = defaultOnvifManager
): Completable =
    awaitDeviceRequestCompletable { om.unsubscribe(this, subscriptionReference, it) }
