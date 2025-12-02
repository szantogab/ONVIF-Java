package be.teletask.onvif.models;

/**
 * Created by Tomas Verhelst on 04/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public enum OnvifType {
    CUSTOM(""),
    GET_SERVICES("http://www.onvif.org/ver10/device/wsdl"),
    GET_DEVICE_INFORMATION("http://www.onvif.org/ver10/device/wsdl"),
    GET_MEDIA_PROFILES("http://www.onvif.org/ver10/media/wsdl"),
    GET_STREAM_URI("http://www.onvif.org/ver10/media/wsdl"),
    GET_SNAPSHOT_URI("http://www.onvif.org/ver10/media/wsdl"),
    CONTINUOUS_MOVE("http://www.onvif.org/ver10/media/wsdl"),
    ABSOLUTE_MOVE("http://www.onvif.org/ver10/media/wsdl"),
    PTZ_STOP("http://www.onvif.org/ver10/media/wsdl"),
    RELATIVE_MOVE("http://www.onvif.org/ver10/media/wsdl"),
    // Motion Detection types
    GET_MOTION_DETECTION_CONFIGURATION("http://www.onvif.org/ver10/analytics/wsdl"),
    SET_MOTION_DETECTION_CONFIGURATION("http://www.onvif.org/ver10/analytics/wsdl"),
    GET_ANALYTICS_ENGINES("http://www.onvif.org/ver10/analytics/wsdl"),
    GET_ANALYTICS_ENGINE_INPUTS("http://www.onvif.org/ver10/analytics/wsdl"),
    CREATE_ANALYTICS_ENGINE_CONTROL("http://www.onvif.org/ver10/analytics/wsdl"),
    DELETE_ANALYTICS_ENGINE_CONTROL("http://www.onvif.org/ver10/analytics/wsdl"),
    GET_EVENT_PROPERTIES("http://www.onvif.org/ver10/events/wsdl"),
    CREATE_PULL_POINT_SUBSCRIPTION("http://www.onvif.org/ver10/events/wsdl"),
    PULL_MESSAGES("http://www.onvif.org/ver10/events/wsdl"),
    UNSUBSCRIBE("http://www.onvif.org/ver10/events/wsdl"),
    // Event Broker types
    ADD_EVENT_BROKER("http://www.onvif.org/ver10/events/wsdl"),
    DELETE_EVENT_BROKER("http://www.onvif.org/ver10/events/wsdl"),
    GET_EVENT_BROKERS("http://www.onvif.org/ver10/events/wsdl");

    public final String namespace;

    OnvifType(String namespace) {
        this.namespace = namespace;
    }

}
