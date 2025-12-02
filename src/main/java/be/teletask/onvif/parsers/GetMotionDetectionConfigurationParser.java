package be.teletask.onvif.parsers;

import be.teletask.onvif.models.OnvifMotionDetection;
import be.teletask.onvif.models.OnvifMotionDetectionConfig;
import be.teletask.onvif.models.OnvifMotionDetectionArea;
import be.teletask.onvif.responses.OnvifResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * ONVIF mozgásérzékelési konfiguráció parser
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class GetMotionDetectionConfigurationParser extends OnvifParser<OnvifMotionDetection> {

    @Override
    public OnvifMotionDetection parse(OnvifResponse response) {
        OnvifMotionDetection motionDetection = new OnvifMotionDetection();
        OnvifMotionDetectionConfig config = new OnvifMotionDetectionConfig();

        try {
            getXpp().setInput(new StringReader(response.getXml()));
            eventType = getXpp().getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = getXpp().getName();
                    
                    switch (tagName) {
                        case "Token":
                            getXpp().next();
                            motionDetection.setToken(getXpp().getText());
                            break;
                        case "Name":
                            getXpp().next();
                            motionDetection.setName(getXpp().getText());
                            break;
                        case "Enabled":
                            getXpp().next();
                            motionDetection.setEnabled(Boolean.parseBoolean(getXpp().getText()));
                            break;
                        case "SourceToken":
                            getXpp().next();
                            motionDetection.setSourceToken(getXpp().getText());
                            break;
                        case "Sensitivity":
                            getXpp().next();
                            config.setSensitivity(Integer.parseInt(getXpp().getText()));
                            break;
                        case "Threshold":
                            getXpp().next();
                            config.setThreshold(Integer.parseInt(getXpp().getText()));
                            break;
                        case "AutoMode":
                            getXpp().next();
                            config.setAutoMode(Boolean.parseBoolean(getXpp().getText()));
                            break;
                        case "MinDetectionTime":
                            getXpp().next();
                            config.setMinDetectionTime(Integer.parseInt(getXpp().getText()));
                            break;
                        case "MaxDetectionTime":
                            getXpp().next();
                            config.setMaxDetectionTime(Integer.parseInt(getXpp().getText()));
                            break;
                        case "DetectionArea":
                            OnvifMotionDetectionArea area = parseDetectionArea();
                            config.setDetectionArea(area);
                            break;
                    }
                }
                
                eventType = getXpp().next();
            }
            
            motionDetection.setConfig(config);
            
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return motionDetection;
    }
    
    private OnvifMotionDetectionArea parseDetectionArea() throws XmlPullParserException, IOException {
        OnvifMotionDetectionArea area = new OnvifMotionDetectionArea();
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = getXpp().getName();
                
                switch (tagName) {
                    case "X":
                        getXpp().next();
                        area.setX(Float.parseFloat(getXpp().getText()));
                        break;
                    case "Y":
                        getXpp().next();
                        area.setY(Float.parseFloat(getXpp().getText()));
                        break;
                    case "Width":
                        getXpp().next();
                        area.setWidth(Float.parseFloat(getXpp().getText()));
                        break;
                    case "Height":
                        getXpp().next();
                        area.setHeight(Float.parseFloat(getXpp().getText()));
                        break;
                    case "Enabled":
                        getXpp().next();
                        area.setEnabled(Boolean.parseBoolean(getXpp().getText()));
                        break;
                }
            } else if (eventType == XmlPullParser.END_TAG && getXpp().getName().equals("DetectionArea")) {
                break;
            }
            
            eventType = getXpp().next();
        }
        
        return area;
    }
}

