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

    val disposable = dev.createPullPointSubscription(
        eventFilters = arrayOf("tns1:RuleEngine/CellMotionDetector/Motion"),
        initialTerminationTimeSeconds = 60,
        pullTimeoutSeconds = 2
    ).subscribe(
        { event ->
            println("Event: $event @ " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
        },
        { err ->
            println("Subscription error: ${err.message}")
        }
    )

    // Rövid ideig futtassuk, majd leiratkozunk (dispose) - ekkor mennie kell az ONVIF Unsubscribe-nak.
    Thread.sleep(5_000)
    disposable.dispose()

    val infos = dev.getInformation().blockingGet()
    val mediaProfiles = dev.getMediaProfiles().blockingGet()
    val snapshotUri = dev.getMediaSnapshotUri(mediaProfiles.first()).blockingGet()

    dev.ptzContinuousMove(mediaProfiles.first().token, -1.0, 1.0, null, 1).blockingAwait()
    dev.ptzRelativeMove(mediaProfiles.first().token, 0.1, 0.0, null).blockingAwait()
    dev.ptzAbsoluteMove(mediaProfiles.first().token, 0.1, 0.0, 1.0).blockingAwait()
    dev.ptzStop(mediaProfiles.first().token).blockingAwait()

    Unit
}
