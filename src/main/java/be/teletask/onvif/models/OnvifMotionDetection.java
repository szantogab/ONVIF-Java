package be.teletask.onvif.models;

/**
 * ONVIF mozgásérzékelési konfiguráció és állapot modell
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class OnvifMotionDetection {

    //Constants
    public static final String TAG = OnvifMotionDetection.class.getSimpleName();

    //Attributes
    private String token;
    private String name;
    private boolean enabled;
    private String sourceToken;
    private OnvifMotionDetectionConfig config;

    //Constructors
    public OnvifMotionDetection() {
    }

    public OnvifMotionDetection(String token, String name, boolean enabled, String sourceToken) {
        this.token = token;
        this.name = name;
        this.enabled = enabled;
        this.sourceToken = sourceToken;
    }

    //Properties
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public void setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
    }

    public OnvifMotionDetectionConfig getConfig() {
        return config;
    }

    public void setConfig(OnvifMotionDetectionConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "OnvifMotionDetection{" +
                "token='" + token + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", sourceToken='" + sourceToken + '\'' +
                ", config=" + config +
                '}';
    }
}

