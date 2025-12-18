package be.teletask.onvif.models;

/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class OnvifMediaProfile {

    //Constants
    public static final String TAG = OnvifMediaProfile.class.getSimpleName();

    //Attributes
    private String name;
    private String token;
    private boolean fixed;

    // VideoSourceConfiguration
    private String videoSourceConfigToken;
    private String videoSourceConfigName;
    private String videoSourceToken;
    private int boundsX;
    private int boundsY;
    private int boundsWidth;
    private int boundsHeight;

    // VideoEncoderConfiguration
    private String videoEncoderConfigToken;
    private String videoEncoderConfigName;
    private String encoding; // H264, JPEG, MPEG4, etc.
    private int width;
    private int height;
    private int quality;
    private int frameRateLimit;
    private int encodingInterval;
    private int bitrateLimit;
    private int govLength;
    private String h264Profile; // Baseline, Main, Extended, High

    // PTZConfiguration
    private String ptzConfigToken;
    private String ptzConfigName;
    private String ptzNodeToken;

    //Constructors

    public OnvifMediaProfile() {
    }

    public OnvifMediaProfile(String name, String token) {
        this.name = name;
        this.token = token;
    }

    //Properties

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    // VideoSourceConfiguration getters/setters

    public String getVideoSourceConfigToken() {
        return videoSourceConfigToken;
    }

    public void setVideoSourceConfigToken(String videoSourceConfigToken) {
        this.videoSourceConfigToken = videoSourceConfigToken;
    }

    public String getVideoSourceConfigName() {
        return videoSourceConfigName;
    }

    public void setVideoSourceConfigName(String videoSourceConfigName) {
        this.videoSourceConfigName = videoSourceConfigName;
    }

    public String getVideoSourceToken() {
        return videoSourceToken;
    }

    public void setVideoSourceToken(String videoSourceToken) {
        this.videoSourceToken = videoSourceToken;
    }

    public int getBoundsX() {
        return boundsX;
    }

    public void setBoundsX(int boundsX) {
        this.boundsX = boundsX;
    }

    public int getBoundsY() {
        return boundsY;
    }

    public void setBoundsY(int boundsY) {
        this.boundsY = boundsY;
    }

    public int getBoundsWidth() {
        return boundsWidth;
    }

    public void setBoundsWidth(int boundsWidth) {
        this.boundsWidth = boundsWidth;
    }

    public int getBoundsHeight() {
        return boundsHeight;
    }

    public void setBoundsHeight(int boundsHeight) {
        this.boundsHeight = boundsHeight;
    }

    // VideoEncoderConfiguration getters/setters

    public String getVideoEncoderConfigToken() {
        return videoEncoderConfigToken;
    }

    public void setVideoEncoderConfigToken(String videoEncoderConfigToken) {
        this.videoEncoderConfigToken = videoEncoderConfigToken;
    }

    public String getVideoEncoderConfigName() {
        return videoEncoderConfigName;
    }

    public void setVideoEncoderConfigName(String videoEncoderConfigName) {
        this.videoEncoderConfigName = videoEncoderConfigName;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getFrameRateLimit() {
        return frameRateLimit;
    }

    public void setFrameRateLimit(int frameRateLimit) {
        this.frameRateLimit = frameRateLimit;
    }

    public int getEncodingInterval() {
        return encodingInterval;
    }

    public void setEncodingInterval(int encodingInterval) {
        this.encodingInterval = encodingInterval;
    }

    public int getBitrateLimit() {
        return bitrateLimit;
    }

    public void setBitrateLimit(int bitrateLimit) {
        this.bitrateLimit = bitrateLimit;
    }

    public int getGovLength() {
        return govLength;
    }

    public void setGovLength(int govLength) {
        this.govLength = govLength;
    }

    public String getH264Profile() {
        return h264Profile;
    }

    public void setH264Profile(String h264Profile) {
        this.h264Profile = h264Profile;
    }

    // PTZConfiguration getters/setters

    public String getPtzConfigToken() {
        return ptzConfigToken;
    }

    public void setPtzConfigToken(String ptzConfigToken) {
        this.ptzConfigToken = ptzConfigToken;
    }

    public String getPtzConfigName() {
        return ptzConfigName;
    }

    public void setPtzConfigName(String ptzConfigName) {
        this.ptzConfigName = ptzConfigName;
    }

    public String getPtzNodeToken() {
        return ptzNodeToken;
    }

    public void setPtzNodeToken(String ptzNodeToken) {
        this.ptzNodeToken = ptzNodeToken;
    }

    // Convenience methods

    /**
     * Returns the resolution as "WIDTHxHEIGHT" string (e.g., "1280x720")
     */
    public String getResolution() {
        return width + "x" + height;
    }

    /**
     * Returns true if this profile has PTZ capabilities
     */
    public boolean hasPtz() {
        return ptzConfigToken != null && !ptzConfigToken.isEmpty();
    }

    @Override
    public String toString() {
        return "OnvifMediaProfile{" +
                "name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", fixed=" + fixed +
                ", encoding='" + encoding + '\'' +
                ", resolution=" + width + "x" + height +
                ", frameRateLimit=" + frameRateLimit +
                ", bitrateLimit=" + bitrateLimit +
                ", quality=" + quality +
                ", h264Profile='" + h264Profile + '\'' +
                ", hasPtz=" + hasPtz() +
                '}';
    }
}
