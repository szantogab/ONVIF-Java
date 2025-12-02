package be.teletask.onvif.listeners;

import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifMotionDetection;
import be.teletask.onvif.models.OnvifMotionEvent;

import java.util.List;

/**
 * ONVIF mozgásérzékelési események listener interfész
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public interface OnvifMotionDetectionListener {

    /**
     * Mozgásérzékelési konfiguráció sikeres lekérdezése
     * @param device ONVIF eszköz
     * @param motionDetection mozgásérzékelési konfiguráció
     */
    void onMotionDetectionConfigurationReceived(OnvifDevice device, OnvifMotionDetection motionDetection);

    /**
     * Mozgásérzékelési konfiguráció sikeres beállítása
     * @param device ONVIF eszköz
     */
    void onMotionDetectionConfigurationSet(OnvifDevice device);

    /**
     * Analytics engine-ek lekérdezése
     * @param device ONVIF eszköz
     * @param analyticsEngines elérhető analytics engine-ek listája
     */
    void onAnalyticsEnginesReceived(OnvifDevice device, List<String> analyticsEngines);

    /**
     * Mozgásérzékelési esemény feliratkozás létrehozva
     * @param device ONVIF eszköz
     * @param subscriptionReference feliratkozás referenciája
     */
    void onMotionDetectionSubscriptionCreated(OnvifDevice device, String subscriptionReference);

    /**
     * Mozgásérzékelési események fogadva
     * @param device ONVIF eszköz
     * @param events mozgásérzékelési események listája
     */
    void onMotionEventsReceived(OnvifDevice device, List<OnvifMotionEvent> events);

    /**
     * Mozgásérzékelési hiba történt
     * @param device ONVIF eszköz
     * @param error hibaüzenet
     */
    void onMotionDetectionError(OnvifDevice device, String error);
}

