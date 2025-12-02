package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifType;

import java.util.List;

/**
 * ONVIF analytics engine-ek lekérdezésére szolgáló kérés
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class GetAnalyticsEnginesRequest implements OnvifRequest<List<String>> {

    //Constants
    public static final String TAG = GetAnalyticsEnginesRequest.class.getSimpleName();

    //Attributes
    private final Listener<List<String>> listener;

    //Constructors
    public GetAnalyticsEnginesRequest(Listener<List<String>> listener) {
        super();
        this.listener = listener;
    }

    //Properties
    public Listener<List<String>> getListener() {
        return listener;
    }

    @Override
    public String getXml() {
        return "<GetAnalyticsEngines xmlns=\"http://www.onvif.org/ver10/analytics/wsdl\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_ANALYTICS_ENGINES;
    }
}

