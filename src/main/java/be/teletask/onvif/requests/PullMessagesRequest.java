package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifMotionEvent;
import be.teletask.onvif.models.OnvifType;

import java.util.List;

/**
 * ONVIF esemény üzenetek lekérdezésére szolgáló kérés
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class PullMessagesRequest implements OnvifRequest<List<OnvifMotionEvent>> {

    //Constants
    public static final String TAG = PullMessagesRequest.class.getSimpleName();

    //Attributes
    private final Listener<List<OnvifMotionEvent>> listener;
    private final String subscriptionReference;
    private final int messageLimit;
    private final int timeout;

    //Constructors
    public PullMessagesRequest(String subscriptionReference, Listener<List<OnvifMotionEvent>> listener) {
        this(subscriptionReference, 20, 60, listener);
    }

    public PullMessagesRequest(String subscriptionReference, int messageLimit, int timeout, Listener<List<OnvifMotionEvent>> listener) {
        super();
        this.subscriptionReference = subscriptionReference;
        this.listener = listener;
        this.messageLimit = messageLimit;
        this.timeout = timeout;
    }

    //Properties
    public Listener<List<OnvifMotionEvent>> getListener() {
        return listener;
    }

    public String getSubscriptionReference() {
        return subscriptionReference;
    }

    public int getMessageLimit() {
        return messageLimit;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public String getSoapHeader() {
        return "<wsa:To xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" + subscriptionReference + "</wsa:To>";
    }

    @Override
    public String getXml() {
        return "<tev:PullMessages>" +
                "<tev:Timeout>PT" + timeout + "S</tev:Timeout>" +
                "<tev:MessageLimit>" + messageLimit + "</tev:MessageLimit>" +
                "</tev:PullMessages>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.PULL_MESSAGES;
    }
}

