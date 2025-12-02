package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifMotionDetection;
import be.teletask.onvif.models.OnvifType;

/**
 * ONVIF mozgásérzékelési konfiguráció lekérdezésére szolgáló kérés
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class GetMotionDetectionConfigurationRequest implements OnvifRequest<OnvifMotionDetection> {

    //Constants
    public static final String TAG = GetMotionDetectionConfigurationRequest.class.getSimpleName();

    //Attributes
    private final Listener<OnvifMotionDetection> listener;
    private final String configurationToken;

    //Constructors
    public GetMotionDetectionConfigurationRequest(Listener<OnvifMotionDetection> listener) {
        this(listener, "MotionDetectionConfig");
    }

    public GetMotionDetectionConfigurationRequest(Listener<OnvifMotionDetection> listener, String configurationToken) {
        super();
        this.listener = listener;
        this.configurationToken = configurationToken;
    }

    //Properties
    public Listener<OnvifMotionDetection> getListener() {
        return listener;
    }

    public String getConfigurationToken() {
        return configurationToken;
    }

    @Override
    public String getXml() {
        return "<GetAnalyticsEngineInput xmlns=\"http://www.onvif.org/ver10/analytics/wsdl\">" +
                "<ConfigurationToken>" + configurationToken + "</ConfigurationToken>" +
                "</GetAnalyticsEngineInput>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_MOTION_DETECTION_CONFIGURATION;
    }
}

