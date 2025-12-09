package be.teletask.onvif.parsers;

import be.teletask.onvif.responses.OnvifResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * ONVIF Event Broker-ek parser
 * Created for event broker management
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class GetEventBrokersParser extends OnvifParser<List<String>> {

    @Override
    public List<String> parse(OnvifResponse response) {
        List<String> eventBrokers = new ArrayList<>();

        try {
            getXpp().setInput(new StringReader(response.getXml()));
            eventType = getXpp().getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && getXpp().getName().equals("Token")) {
                    getXpp().next();
                    String token = getXpp().getText();
                    if (token != null && !token.isEmpty()) {
                        eventBrokers.add(token);
                    }
                }
                
                eventType = getXpp().next();
            }
            
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return eventBrokers;
    }
}

