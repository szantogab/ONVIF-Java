package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifType;

import java.util.List;

/**
 * ONVIF Event Broker-ek lekérdezésére szolgáló kérés
 * Created for event broker management
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class GetEventBrokersRequest implements OnvifRequest<List<String>> {

    //Constants
    public static final String TAG = GetEventBrokersRequest.class.getSimpleName();

    //Attributes
    private final Listener<List<String>> listener;

    //Constructors
    public GetEventBrokersRequest(Listener<List<String>> listener) {
        super();
        this.listener = listener;
    }

    //Properties
    public Listener<List<String>> getListener() {
        return listener;
    }

    @Override
    public String getXml() {
        return "<GetEventBrokers xmlns=\"http://www.onvif.org/ver10/events/wsdl\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_EVENT_BROKERS;
    }
}
