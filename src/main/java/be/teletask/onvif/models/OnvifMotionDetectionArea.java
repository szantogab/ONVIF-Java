package be.teletask.onvif.models;

/**
 * ONVIF mozgásérzékelési terület definíció
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class OnvifMotionDetectionArea {

    //Constants
    public static final String TAG = OnvifMotionDetectionArea.class.getSimpleName();

    //Attributes
    private float x; // 0.0 - 1.0
    private float y; // 0.0 - 1.0
    private float width; // 0.0 - 1.0
    private float height; // 0.0 - 1.0
    private boolean enabled;

    //Constructors
    public OnvifMotionDetectionArea() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.width = 1.0f;
        this.height = 1.0f;
        this.enabled = true;
    }

    public OnvifMotionDetectionArea(float x, float y, float width, float height) {
        this.x = Math.max(0.0f, Math.min(1.0f, x));
        this.y = Math.max(0.0f, Math.min(1.0f, y));
        this.width = Math.max(0.0f, Math.min(1.0f, width));
        this.height = Math.max(0.0f, Math.min(1.0f, height));
        this.enabled = true;
    }

    //Properties
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = Math.max(0.0f, Math.min(1.0f, x));
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = Math.max(0.0f, Math.min(1.0f, y));
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = Math.max(0.0f, Math.min(1.0f, width));
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = Math.max(0.0f, Math.min(1.0f, height));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "OnvifMotionDetectionArea{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", enabled=" + enabled +
                '}';
    }
}

