package be.teletask.onvif

import be.teletask.onvif.coroutines.*
import be.teletask.onvif.models.OnvifDevice
import be.teletask.onvif.models.OnvifDeviceInformation
import be.teletask.onvif.models.OnvifMediaProfile
import be.teletask.onvif.models.OnvifMotionDetection
import be.teletask.onvif.models.OnvifMotionEvent
import be.teletask.onvif.models.OnvifEventProperties

val defaultOnvifManager by lazy { OnvifManager() }

suspend fun OnvifDevice.getInformation(om: OnvifManager = defaultOnvifManager) = awaitDeviceRequest<OnvifDeviceInformation> { om.getDeviceInformation(this, it) }

suspend fun OnvifDevice.getMediaProfiles(om: OnvifManager = defaultOnvifManager) = awaitDeviceRequest<List<OnvifMediaProfile>> { om.getMediaProfiles(this, it) }

suspend fun OnvifDevice.getMediaStreamUri(profile: OnvifMediaProfile, om: OnvifManager = defaultOnvifManager) = awaitDeviceRequest<String> { om.getMediaStreamURI(this, profile, it) }

suspend fun OnvifDevice.getMediaSnapshotUri(profile: OnvifMediaProfile, om: OnvifManager = defaultOnvifManager) = awaitDeviceRequest<String> { om.getMediaSnapshotURI(this, profile, it) }

suspend fun OnvifDevice.getAllMediaStreamUris(om: OnvifManager = defaultOnvifManager) = getMediaProfiles(om).map { getMediaStreamUri(it, om) }

suspend fun OnvifDevice.getAllMediaSnapshotUris(om: OnvifManager = defaultOnvifManager) = getMediaProfiles(om).map { getMediaSnapshotUri(it, om) }

suspend fun OnvifDevice.ptzContinuousMove(profileToken: String, velocityX: Double, velocityY: Double, velocityZ: Double?, timeout: Int?, om: OnvifManager = defaultOnvifManager) = awaitDeviceRequest<Void> { om.ptzContinuousMove(this, profileToken, velocityX, velocityY, velocityZ, timeout, it) }

suspend fun OnvifDevice.ptzRelativeMove(profileToken: String, translationX: Double?, translationY: Double?, zoom: Double?, om: OnvifManager = defaultOnvifManager) = awaitDeviceRequest<Void> { om.ptzRelativeMove(this, profileToken, translationX, translationY, zoom, it) }

suspend fun OnvifDevice.ptzAbsoluteMove(profileToken: String, positionX: Double?, positionY: Double?, zoom: Double?, om: OnvifManager = defaultOnvifManager) = awaitDeviceRequest<Void> { om.ptzAbsoluteMove(this, profileToken, positionX, positionY, zoom, it) }

suspend fun OnvifDevice.ptzStop(profileToken: String, panTilt: Boolean = true, zoom: Boolean = true, om: OnvifManager = defaultOnvifManager) = awaitDeviceRequest<Void> { om.ptzStop(this, profileToken, panTilt, zoom, it) }

// Motion detection extensions

suspend fun OnvifDevice.getMotionDetectionConfiguration(om: OnvifManager = defaultOnvifManager) =
    awaitDeviceRequest<OnvifMotionDetection> { om.getMotionDetectionConfiguration(this, it) }

suspend fun OnvifDevice.setMotionDetectionConfiguration(
    motionDetection: OnvifMotionDetection,
    om: OnvifManager = defaultOnvifManager
) = awaitDeviceRequest<Void> { om.setMotionDetectionConfiguration(this, motionDetection, it) }

suspend fun OnvifDevice.getAnalyticsEngines(om: OnvifManager = defaultOnvifManager) =
    awaitDeviceRequest<List<String>> { om.getAnalyticsEngines(this, it) }

suspend fun OnvifDevice.createPullPointSubscription(eventFilters: Array<String>? = null, initialTerminationTimeSeconds: Int = 60, om: OnvifManager = defaultOnvifManager) =
    awaitDeviceRequest<String> { om.createPullPointSubscription(this, eventFilters, initialTerminationTimeSeconds, it) }

suspend fun OnvifDevice.pullMessages(
    subscriptionReference: String,
    messageLimit: Int = 20,
    timeoutSeconds: Int = 60,
    om: OnvifManager = defaultOnvifManager
) = awaitDeviceRequest<List<OnvifMotionEvent>> { om.pullMessages(this, subscriptionReference, messageLimit, timeoutSeconds,it) }

// Events – GetEventProperties

suspend fun OnvifDevice.getEventProperties(om: OnvifManager = defaultOnvifManager) =
    awaitDeviceRequest<OnvifEventProperties> { om.getEventProperties(this, it) }

// Event Broker extensions

suspend fun OnvifDevice.addEventBroker(
    brokerAddress: String,
    om: OnvifManager = defaultOnvifManager
) = awaitDeviceRequest<Void> { om.addEventBroker(this, brokerAddress, it) }

suspend fun OnvifDevice.deleteEventBroker(
    brokerToken: String,
    om: OnvifManager = defaultOnvifManager
) = awaitDeviceRequest<Void> { om.deleteEventBroker(this, brokerToken, it) }

suspend fun OnvifDevice.getEventBrokers(om: OnvifManager = defaultOnvifManager) =
    awaitDeviceRequest<List<String>> { om.getEventBrokers(this, it) }

// Unsubscribe extension

suspend fun OnvifDevice.unsubscribe(
    subscriptionReference: String,
    om: OnvifManager = defaultOnvifManager
) = awaitDeviceRequest<Void> { om.unsubscribe(this, subscriptionReference, it) }
