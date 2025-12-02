package be.teletask.onvif.parsers;

import be.teletask.onvif.responses.OnvifResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * ONVIF pull point subscription parser
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class CreatePullPointSubscriptionParser extends OnvifParser<String> {

    @Override
    public String parse(OnvifResponse response) {
        String subscriptionReference = null;

        try {
            getXpp().setInput(new StringReader(response.getXml()));
            eventType = getXpp().getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && getXpp().getName().equals("Address")) {
                    getXpp().next();
                    subscriptionReference = getXpp().getText();
                    break;
                }
                
                eventType = getXpp().next();
            }
            
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return subscriptionReference;
    }
}

