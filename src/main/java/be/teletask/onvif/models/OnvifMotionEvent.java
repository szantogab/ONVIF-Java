package be.teletask.onvif.models;

import java.util.Date;

/**
 * ONVIF mozgásérzékelési esemény modell
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class OnvifMotionEvent {

    //Constants
    public static final String TAG = OnvifMotionEvent.class.getSimpleName();

    //Attributes
    private String eventId;
    private String sourceToken;
    private Date timestamp;
    private OnvifMotionDetectionArea detectionArea;
    private float confidence; // 0.0 - 1.0
    private String eventType; // "MotionDetected", "MotionStopped"
    private String message;

    //Constructors
    public OnvifMotionEvent() {
        this.timestamp = new Date();
    }

    public OnvifMotionEvent(String eventId, String sourceToken, String eventType) {
        this.eventId = eventId;
        this.sourceToken = sourceToken;
        this.eventType = eventType;
        this.timestamp = new Date();
    }

    //Properties
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public void setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public OnvifMotionDetectionArea getDetectionArea() {
        return detectionArea;
    }

    public void setDetectionArea(OnvifMotionDetectionArea detectionArea) {
        this.detectionArea = detectionArea;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = Math.max(0.0f, Math.min(1.0f, confidence));
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OnvifMotionEvent{" +
                "eventId='" + eventId + '\'' +
                ", sourceToken='" + sourceToken + '\'' +
                ", timestamp=" + timestamp +
                ", detectionArea=" + detectionArea +
                ", confidence=" + confidence +
                ", eventType='" + eventType + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

