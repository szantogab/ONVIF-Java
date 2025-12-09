package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifType;

/**
 * ONVIF Event Broker törlésére szolgáló kérés
 * Created for event broker management
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class DeleteEventBrokerRequest implements OnvifRequest<Void> {

    //Constants
    public static final String TAG = DeleteEventBrokerRequest.class.getSimpleName();

    //Attributes
    private final Listener<Void> listener;
    private final String brokerToken;

    //Constructors
    public DeleteEventBrokerRequest(String brokerToken, Listener<Void> listener) {
        super();
        this.brokerToken = brokerToken;
        this.listener = listener;
    }

    //Properties
    public Listener<Void> getListener() {
        return listener;
    }

    public String getBrokerToken() {
        return brokerToken;
    }

    @Override
    public String getXml() {
        return "<DeleteEventBroker xmlns=\"http://www.onvif.org/ver10/events/wsdl\">" +
                "<BrokerToken>" + brokerToken + "</BrokerToken>" +
                "</DeleteEventBroker>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.DELETE_EVENT_BROKER;
    }
}

