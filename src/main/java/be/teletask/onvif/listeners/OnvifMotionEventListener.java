package be.teletask.onvif.listeners;

import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifMotionEvent;

/**
 * ONVIF mozgásérzékelési esemény listener interfész
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public interface OnvifMotionEventListener {

    /**
     * Mozgásérzékelési esemény fogadva
     * @param device ONVIF eszköz
     * @param event mozgásérzékelési esemény
     */
    void onMotionEvent(OnvifDevice device, OnvifMotionEvent event);

    /**
     * Mozgásérzékelési esemény hiba
     * @param device ONVIF eszköz
     * @param error hibaüzenet
     */
    void onMotionEventError(OnvifDevice device, String error);
}

