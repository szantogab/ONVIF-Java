# ONVIF-Java
---
[ ![Download](https://api.bintray.com/packages/tomasverhelst/ONVIF-Java/ONVIF-Java/images/download.svg) ](https://bintray.com/tomasverhelst/ONVIF-Java/ONVIF-Java/_latestVersion)

<p align="center"> 
<img src="https://botw-pd.s3.amazonaws.com/styles/logo-thumbnail/s3/112012/onvif-converted.png?itok=yqR6_a6G">
</p>

ONVIF is an open industry forum that provides and promotes standardized interfaces for effective interoperability of IP-based physical security products. ONVIF was created to make a standard way of how IP products within CCTV and other security areas can communicate with each other.

## RxJava3

All the Kotlin functions provided are RxJava3 `Single`/`Completable` functions.
For a simple synchronous example you can use `.blockingGet()` / `.blockingAwait()`.

### Discovery
Straight forward solution:

```kotlin
val devices: List<Device> = discoverDevices().blockingGet()
```

If you need to create and customize a a one-shot `DiscoveryManager`:
```kotlin
val devices = discoverDevices {
    discoveryTimeout = 10000
}.blockingGet()
```

If using a custom `DiscoveryManager`:
```kotlin
    val myDM = DiscoveryManager().apply { discoveryTimeout = 10000 }
    val devices = awaitDeviceDiscovery { myDM.discover(it) }.blockingGet()
```

### Information, Profiles and URIs

Once you obtained the device host and you know username and password, create the `OnvifDevice` and get its data async using:

```kotlin
val om = OnvifManager()
val device = OnvifDevice(host, user, pswd)

val info = device.getInformation(om).blockingGet()
val profiles = device.getMediaProfiles(om).blockingGet()
val streamUri = device.getMediaStreamUri(profiles.first(), om).blockingGet()
val snapshotUri = device.getMediaSnapshotUri(profiles.first(), om).blockingGet()
```

Or even more easy:
```kotlin
val device = OnvifDevice(host, user, pswd)
val info = device.getInformation().blockingGet()
val mediaProfiles = device.getMediaProfiles().blockingGet()
val aStreamUri = device.getMediaStreamUri(mediaProfiles.first()).blockingGet()
val aSnapshotUri = device.getMediaSnapshotUri(mediaProfiles.first()).blockingGet()
```
