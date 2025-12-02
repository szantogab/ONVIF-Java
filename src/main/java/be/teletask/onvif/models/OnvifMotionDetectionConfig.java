package be.teletask.onvif.models;

/**
 * ONVIF mozgásérzékelési konfigurációs paraméterek
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class OnvifMotionDetectionConfig {

    //Constants
    public static final String TAG = OnvifMotionDetectionConfig.class.getSimpleName();

    //Attributes
    private int sensitivity; // 1-100
    private int threshold; // 1-100
    private OnvifMotionDetectionArea detectionArea;
    private boolean autoMode;
    private int minDetectionTime; // milliseconds
    private int maxDetectionTime; // milliseconds

    //Constructors
    public OnvifMotionDetectionConfig() {
        this.sensitivity = 50;
        this.threshold = 50;
        this.autoMode = true;
        this.minDetectionTime = 100;
        this.maxDetectionTime = 5000;
    }

    public OnvifMotionDetectionConfig(int sensitivity, int threshold, OnvifMotionDetectionArea detectionArea) {
        this.sensitivity = sensitivity;
        this.threshold = threshold;
        this.detectionArea = detectionArea;
        this.autoMode = true;
        this.minDetectionTime = 100;
        this.maxDetectionTime = 5000;
    }

    //Properties
    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = Math.max(1, Math.min(100, sensitivity));
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = Math.max(1, Math.min(100, threshold));
    }

    public OnvifMotionDetectionArea getDetectionArea() {
        return detectionArea;
    }

    public void setDetectionArea(OnvifMotionDetectionArea detectionArea) {
        this.detectionArea = detectionArea;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }

    public int getMinDetectionTime() {
        return minDetectionTime;
    }

    public void setMinDetectionTime(int minDetectionTime) {
        this.minDetectionTime = Math.max(0, minDetectionTime);
    }

    public int getMaxDetectionTime() {
        return maxDetectionTime;
    }

    public void setMaxDetectionTime(int maxDetectionTime) {
        this.maxDetectionTime = Math.max(minDetectionTime, maxDetectionTime);
    }

    @Override
    public String toString() {
        return "OnvifMotionDetectionConfig{" +
                "sensitivity=" + sensitivity +
                ", threshold=" + threshold +
                ", detectionArea=" + detectionArea +
                ", autoMode=" + autoMode +
                ", minDetectionTime=" + minDetectionTime +
                ", maxDetectionTime=" + maxDetectionTime +
                '}';
    }
}

