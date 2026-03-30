package be.teletask.onvif

import be.teletask.onvif.rxjava.discoverDevices
import be.teletask.onvif.models.OnvifDevice
import be.teletask.onvif.models.OnvifMotionEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    val onvifDevices = discoverDevices {
        discoveryMode = DiscoveryMode.ONVIF
        discoveryTimeout = 2000
    }.blockingGet().filterIsInstance<OnvifDevice>()

    /*val dev = onvifDevices.first()*/
    val dev = OnvifDevice("192.168.0.142:2020", "smartive", "smartive1")
    val ae = dev.getEventProperties().blockingGet()

    val sub = dev.createPullPointSubscription(
        arrayOf("tns1:RuleEngine/CellMotionDetector/Motion"),
        initialTerminationTimeSeconds = 60
    ).blockingGet()
    // val sub = dev.createPullPointSubscription(arrayOf("tns1:VideoSource/MotionAlarm"))

    val events = mutableListOf<OnvifMotionEvent>()

    for (i in 0..5) {
        println("Pulling events.." + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))

        val newEvents = dev.pullMessages(sub, timeoutSeconds = 60).blockingGet()
        events.addAll(newEvents)
        println("Pulled events: " + newEvents.joinToString(separator = "\n") { it.toString() })
        Thread.sleep(100)
    }

    dev.unsubscribe(sub).blockingAwait()

    val infos = dev.getInformation().blockingGet()
    val mediaProfiles = dev.getMediaProfiles().blockingGet()
    val snapshotUri = dev.getMediaSnapshotUri(mediaProfiles.first()).blockingGet()

    dev.ptzContinuousMove(mediaProfiles.first().token, -1.0, 1.0, null, 1).blockingAwait()
    dev.ptzRelativeMove(mediaProfiles.first().token, 0.1, 0.0, null).blockingAwait()
    dev.ptzAbsoluteMove(mediaProfiles.first().token, 0.1, 0.0, 1.0).blockingAwait()
    dev.ptzStop(mediaProfiles.first().token).blockingAwait()

    Unit
}
