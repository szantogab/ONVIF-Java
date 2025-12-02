package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifType;

/**
 * ONVIF esemény feliratkozás létrehozására szolgáló kérés
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class CreatePullPointSubscriptionRequest implements OnvifRequest<String> {

    //Constants
    public static final String TAG = CreatePullPointSubscriptionRequest.class.getSimpleName();

    //Attributes
    private final Listener<String> listener;
    private final int initialTerminationTimeSeconds;

    private final String[] eventFilters;

    //Constructors
    public CreatePullPointSubscriptionRequest(Listener<String> listener) {
        this(listener, null, 60);
    }

    public CreatePullPointSubscriptionRequest(Listener<String> listener, String[] eventFilters, int initialTerminationTimeSeconds) {
        super();
        this.listener = listener;
        this.eventFilters = eventFilters;
        this.initialTerminationTimeSeconds = initialTerminationTimeSeconds;
    }

    //Properties
    public Listener<String> getListener() {
        return listener;
    }

    public String[] getEventFilters() {
        return eventFilters;
    }

    @Override
    public String getXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<tev:CreatePullPointSubscription>");
        
        if (eventFilters != null && eventFilters.length > 0) {
            xml.append("<tev:Filter>");

            for (String filter : eventFilters) {
                xml.append("<tev:TopicExpression Dialect=\"http://www.onvif.org/ver10/tev/topicExpression/ConcreteSet\">");
                //xml.append("<tev:TopicExpression Dialect=\"http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete\">");
                xml.append(filter);
                xml.append("</tev:TopicExpression>");
            }

            xml.append("</tev:Filter>");
        }

        xml.append("<tev:InitialTerminationTime>PT" + initialTerminationTimeSeconds + "S</tev:InitialTerminationTime>");
        xml.append("</tev:CreatePullPointSubscription>");
        
        return xml.toString();
    }

    @Override
    public OnvifType getType() {
        return OnvifType.CREATE_PULL_POINT_SUBSCRIPTION;
    }
}

