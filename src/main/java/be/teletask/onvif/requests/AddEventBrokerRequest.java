package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifType;

/**
 * ONVIF Event Broker hozzáadására szolgáló kérés
 * Created for event broker management
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class AddEventBrokerRequest implements OnvifRequest<Void> {

    //Constants
    public static final String TAG = AddEventBrokerRequest.class.getSimpleName();

    //Attributes
    private final Listener<Void> listener;
    private final String brokerAddress;

    //Constructors
    public AddEventBrokerRequest(String brokerAddress, Listener<Void> listener) {
        super();
        this.brokerAddress = brokerAddress;
        this.listener = listener;
    }

    //Properties
    public Listener<Void> getListener() {
        return listener;
    }

    public String getBrokerAddress() {
        return brokerAddress;
    }

    @Override
    public String getXml() {
        return "<AddEventBroker xmlns=\"http://www.onvif.org/ver10/events/wsdl\">" +
                "<Broker>" +
                "<Address>" + brokerAddress + "</Address>" +
                "</Broker>" +
                "</AddEventBroker>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.ADD_EVENT_BROKER;
    }
}





