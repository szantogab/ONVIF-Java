package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifMotionDetection;
import be.teletask.onvif.models.OnvifMotionDetectionConfig;
import be.teletask.onvif.models.OnvifType;

/**
 * ONVIF mozgásérzékelési konfiguráció beállítására szolgáló kérés
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class SetMotionDetectionConfigurationRequest implements OnvifRequest<Void> {

    //Constants
    public static final String TAG = SetMotionDetectionConfigurationRequest.class.getSimpleName();

    //Attributes
    private final Listener<Void> listener;
    private final OnvifMotionDetection motionDetection;

    //Constructors
    public SetMotionDetectionConfigurationRequest(OnvifMotionDetection motionDetection, Listener<Void> listener) {
        super();
        this.motionDetection = motionDetection;
        this.listener = listener;
    }

    //Properties
    public Listener<Void> getListener() {
        return listener;
    }

    public OnvifMotionDetection getMotionDetection() {
        return motionDetection;
    }

    @Override
    public String getXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<SetAnalyticsEngineInput xmlns=\"http://www.onvif.org/ver10/analytics/wsdl\">");
        xml.append("<ConfigurationToken>").append(motionDetection.getToken()).append("</ConfigurationToken>");
        xml.append("<AnalyticsEngineInput>");
        
        if (motionDetection.getConfig() != null) {
            OnvifMotionDetectionConfig config = motionDetection.getConfig();
            xml.append("<SourceIdentification>");
            xml.append("<SourceToken>").append(motionDetection.getSourceToken()).append("</SourceToken>");
            xml.append("</SourceIdentification>");
            xml.append("<VideoAnalyticsConfiguration>");
            xml.append("<AnalyticsEngineConfiguration>");
            xml.append("<Parameters>");
            xml.append("<SimpleItem Name=\"Sensitivity\" Value=\"").append(config.getSensitivity()).append("\"/>");
            xml.append("<SimpleItem Name=\"Threshold\" Value=\"").append(config.getThreshold()).append("\"/>");
            xml.append("<SimpleItem Name=\"AutoMode\" Value=\"").append(config.isAutoMode()).append("\"/>");
            xml.append("<SimpleItem Name=\"MinDetectionTime\" Value=\"").append(config.getMinDetectionTime()).append("\"/>");
            xml.append("<SimpleItem Name=\"MaxDetectionTime\" Value=\"").append(config.getMaxDetectionTime()).append("\"/>");
            xml.append("</Parameters>");
            xml.append("</AnalyticsEngineConfiguration>");
            xml.append("</VideoAnalyticsConfiguration>");
        }
        
        xml.append("</AnalyticsEngineInput>");
        xml.append("</SetAnalyticsEngineInput>");
        
        return xml.toString();
    }

    @Override
    public OnvifType getType() {
        return OnvifType.SET_MOTION_DETECTION_CONFIGURATION;
    }
}

