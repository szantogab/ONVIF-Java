package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifType;

import java.util.List;

/**
 * ONVIF Event Broker lekérdezésére szolgáló kérés (singularis verzió)
 * Egyes kamerák ezt a verziót támogatják a GetEventBrokers helyett.
 * Created for event broker management
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class GetEventBrokerRequest implements OnvifRequest<List<String>> {

    //Constants
    public static final String TAG = GetEventBrokerRequest.class.getSimpleName();

    //Attributes
    private final Listener<List<String>> listener;

    //Constructors
    public GetEventBrokerRequest(Listener<List<String>> listener) {
        super();
        this.listener = listener;
    }

    //Properties
    public Listener<List<String>> getListener() {
        return listener;
    }

    @Override
    public String getXml() {
        // Singularis verzió - egyes kamerák ezt támogatják
        return "<GetEventBroker xmlns=\"http://www.onvif.org/ver10/events/wsdl\"/>";
    }

    @Override
    public OnvifType getType() {
        // Ugyanazt a típust használjuk, mert ugyanazt a funkciót látja el
        return OnvifType.GET_EVENT_BROKERS;
    }
}

