package be.teletask.onvif

import be.teletask.onvif.coroutines.discoverDevices
import be.teletask.onvif.models.OnvifDevice
import be.teletask.onvif.models.OnvifMotionEvent
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() = runBlocking {
   val onvifDevices = discoverDevices { discoveryMode = DiscoveryMode.ONVIF; discoveryTimeout = 2000; }.filterIsInstance<OnvifDevice>()

	val dev = OnvifDevice("192.168.0.119:8000", "admin", "admin")
	val ae = dev.getEventProperties()
	val sub = dev.createPullPointSubscription(arrayOf("tns1:RuleEngine/CellMotionDetector/Motion"), initialTerminationTimeSeconds = 60)
	//val sub = dev.createPullPointSubscription(arrayOf("tns1:VideoSource/MotionAlarm"))

	val events = mutableListOf<OnvifMotionEvent>()

	for (i in 0..5) {
		println("Pulling events.." + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
		val newEvents = dev.pullMessages(sub, timeoutSeconds = 60)
		events.addAll(newEvents)
		println("Pulled events: " + newEvents.joinToString(separator = "\n") { it.toString() })
		Thread.sleep(100)
	}

	dev.unsubscribe(sub)

	val i = dev.getInformation()
	val mediaProfiles = dev.getMediaProfiles()
	val snapshotUri = dev.getMediaSnapshotUri(mediaProfiles.first())

	dev.ptzContinuousMove(mediaProfiles.first().token, -1.0, 1.0, null, 1)
	dev.ptzRelativeMove(mediaProfiles.first().token, 0.1, 0.0, null)
	dev.ptzAbsoluteMove(mediaProfiles.first().token, 0.1, 0.0, 1.0)
	dev.ptzStop(mediaProfiles.first().token)

    Unit
}
