# ONVIF Mozgásérzékelési Képességek

Ez a dokumentum leírja az ONVIF Java könyvtárhoz hozzáadott mozgásérzékelési funkciókat.

## Áttekintés

A mozgásérzékelési képességek lehetővé teszik:
- Mozgásérzékelési konfiguráció lekérdezését és beállítását
- Analytics engine-ek kezelését
- Mozgásérzékelési események feliratkozását és fogadását
- Valós idejű mozgásérzékelési értesítések kezelését

## Új Osztályok

### Modell Osztályok

#### OnvifMotionDetection
A mozgásérzékelési konfiguráció fő modellje.

```java
OnvifMotionDetection motionDetection = new OnvifMotionDetection();
motionDetection.setToken("MotionDetectionConfig");
motionDetection.setName("Alapértelmezett mozgásérzékelés");
motionDetection.setEnabled(true);
motionDetection.setSourceToken("VideoSource_1");
```

#### OnvifMotionDetectionConfig
A mozgásérzékelési konfigurációs paraméterek.

```java
OnvifMotionDetectionConfig config = new OnvifMotionDetectionConfig();
config.setSensitivity(75); // 1-100
config.setThreshold(60);   // 1-100
config.setAutoMode(true);
config.setMinDetectionTime(200);  // milliszekundum
config.setMaxDetectionTime(3000); // milliszekundum
```

#### OnvifMotionDetectionArea
A mozgásérzékelési terület definíciója.

```java
OnvifMotionDetectionArea area = new OnvifMotionDetectionArea(0.0f, 0.0f, 1.0f, 1.0f);
// x, y, width, height (0.0 - 1.0 közötti értékek)
```

#### OnvifMotionEvent
A mozgásérzékelési esemény modellje.

```java
OnvifMotionEvent event = new OnvifMotionEvent();
event.setEventType("MotionDetected"); // vagy "MotionStopped"
event.setTimestamp(new Date());
event.setConfidence(0.85f); // 0.0 - 1.0
```

### Kérés Osztályok

#### GetMotionDetectionConfigurationRequest
Mozgásérzékelési konfiguráció lekérdezése.

```java
GetMotionDetectionConfigurationRequest request = 
    new GetMotionDetectionConfigurationRequest(listener);
```

#### SetMotionDetectionConfigurationRequest
Mozgásérzékelési konfiguráció beállítása.

```java
SetMotionDetectionConfigurationRequest request = 
    new SetMotionDetectionConfigurationRequest(motionDetection, listener);
```

#### GetAnalyticsEnginesRequest
Analytics engine-ek lekérdezése.

```java
GetAnalyticsEnginesRequest request = new GetAnalyticsEnginesRequest(listener);
```

#### CreatePullPointSubscriptionRequest
Esemény feliratkozás létrehozása.

```java
CreatePullPointSubscriptionRequest request = 
    new CreatePullPointSubscriptionRequest(listener);
```

#### PullMessagesRequest
Esemény üzenetek lekérdezése.

```java
PullMessagesRequest request = 
    new PullMessagesRequest(subscriptionReference, listener);
```

### Listener Interfészek

#### OnvifMotionDetectionListener
Mozgásérzékelési események általános listener-e.

```java
public interface OnvifMotionDetectionListener {
    void onMotionDetectionConfigurationReceived(OnvifDevice device, OnvifMotionDetection motionDetection);
    void onMotionDetectionConfigurationSet(OnvifDevice device);
    void onAnalyticsEnginesReceived(OnvifDevice device, List<String> analyticsEngines);
    void onMotionDetectionSubscriptionCreated(OnvifDevice device, String subscriptionReference);
    void onMotionEventsReceived(OnvifDevice device, List<OnvifMotionEvent> events);
    void onMotionDetectionError(OnvifDevice device, String error);
}
```

#### OnvifMotionEventListener
Mozgásérzékelési események specifikus listener-e.

```java
public interface OnvifMotionEventListener {
    void onMotionEvent(OnvifDevice device, OnvifMotionEvent event);
    void onMotionEventError(OnvifDevice device, String error);
}
```

## Használat

### Alapvető Használat

```java
OnvifManager onvifManager = new OnvifManager();
OnvifDevice device = new OnvifDevice("192.168.1.100", "admin", "password");

// Mozgásérzékelési konfiguráció lekérdezése
onvifManager.getMotionDetectionConfiguration(device, new OnvifRequest.Listener<OnvifMotionDetection>() {
    @Override
    public void onSuccess(OnvifDevice device, OnvifMotionDetection motionDetection) {
        System.out.println("Konfiguráció: " + motionDetection.getName());
    }

    @Override
    public void onError(OnvifRequest.OnvifException exception) {
        System.err.println("Hiba: " + exception.getMessage());
    }
});
```

### Mozgásérzékelési Konfiguráció Beállítása

```java
// Mozgásérzékelési terület létrehozása
OnvifMotionDetectionArea detectionArea = new OnvifMotionDetectionArea(0.0f, 0.0f, 1.0f, 1.0f);

// Konfiguráció létrehozása
OnvifMotionDetectionConfig config = new OnvifMotionDetectionConfig();
config.setSensitivity(75);
config.setThreshold(60);
config.setDetectionArea(detectionArea);

// Mozgásérzékelési objektum létrehozása
OnvifMotionDetection motionDetection = new OnvifMotionDetection();
motionDetection.setToken("MotionDetectionConfig");
motionDetection.setName("Alapértelmezett mozgásérzékelés");
motionDetection.setEnabled(true);
motionDetection.setSourceToken("VideoSource_1");
motionDetection.setConfig(config);

// Konfiguráció beállítása
onvifManager.setMotionDetectionConfiguration(device, motionDetection, new OnvifRequest.Listener<Void>() {
    @Override
    public void onSuccess(OnvifDevice device, Void result) {
        System.out.println("Konfiguráció beállítva!");
    }

    @Override
    public void onError(OnvifRequest.OnvifException exception) {
        System.err.println("Hiba: " + exception.getMessage());
    }
});
```

### Esemény Feliratkozás és Figyelés

```java
// Feliratkozás létrehozása
onvifManager.createMotionDetectionSubscription(device, new OnvifRequest.Listener<String>() {
    @Override
    public void onSuccess(OnvifDevice device, String subscriptionRef) {
        // Események lekérdezése
        onvifManager.pullMotionEvents(device, subscriptionRef, new OnvifRequest.Listener<List<OnvifMotionEvent>>() {
            @Override
            public void onSuccess(OnvifDevice device, List<OnvifMotionEvent> events) {
                for (OnvifMotionEvent event : events) {
                    System.out.println("Esemény: " + event.getEventType() + " - " + event.getTimestamp());
                }
            }

            @Override
            public void onError(OnvifRequest.OnvifException exception) {
                System.err.println("Hiba: " + exception.getMessage());
            }
        });
    }

    @Override
    public void onError(OnvifRequest.OnvifException exception) {
        System.err.println("Hiba: " + exception.getMessage());
    }
});
```

## Teljes Példa

Lásd a `MotionDetectionExample.java` fájlt a `examples` csomagban a teljes használati példáért.

## Megjegyzések

1. **Kompatibilitás**: A mozgásérzékelési funkciók csak olyan ONVIF eszközökkel működnek, amelyek támogatják az Analytics és Events szolgáltatásokat.

2. **Szolgáltatás Útvonalak**: Az `OnvifServices` osztály automatikusan kezeli az analytics és events szolgáltatások útvonalait.

3. **Esemény Szűrés**: A `CreatePullPointSubscriptionRequest` alapértelmezetten a `tns1:VideoSource/MotionAlarm` eseményekre szűr.

4. **Hibakezelés**: Minden kérés tartalmaz hibakezelést az `OnvifRequest.OnvifException` segítségével.

5. **Aszinkron Működés**: Minden kérés aszinkron módon fut, callback-ekkel kezelve a válaszokat.

## Támogatott ONVIF Szolgáltatások

- **Analytics Service**: Mozgásérzékelési konfiguráció kezelése
- **Events Service**: Esemény feliratkozás és üzenetek lekérdezése
- **Device Service**: Alapvető eszköz információk (már meglévő)

## Függőségek

A mozgásérzékelési funkciók ugyanazokat a függőségeket használják, mint a meglévő ONVIF könyvtár:
- OkHttp (HTTP kliens)
- XML Pull Parser (XML feldolgozás)
- Digest Authentication (biztonságos hitelesítés)

