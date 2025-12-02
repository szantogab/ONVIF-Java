package be.teletask.onvif.parsers;

import be.teletask.onvif.models.OnvifMotionEvent;
import be.teletask.onvif.models.OnvifMotionDetectionArea;
import be.teletask.onvif.responses.OnvifResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ONVIF pull messages parser
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class PullMessagesParser extends OnvifParser<List<OnvifMotionEvent>> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    @Override
    public List<OnvifMotionEvent> parse(OnvifResponse response) {
        List<OnvifMotionEvent> events = new ArrayList<>();

        try {
            getXpp().setInput(new StringReader(response.getXml()));
            eventType = getXpp().getEventType();
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // A tényleges válaszban wsnt:NotificationMessage elemek vannak
                if (eventType == XmlPullParser.START_TAG && 
                    (getXpp().getName().equals("NotificationMessage") || 
                     getXpp().getName().endsWith(":NotificationMessage"))) {
                    OnvifMotionEvent event = parseNotificationMessage();
                    if (event != null) {
                        events.add(event);
                    }
                }
                
                eventType = getXpp().next();
            }
            
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return events;
    }
    
    private OnvifMotionEvent parseNotificationMessage() throws XmlPullParserException, IOException {
        OnvifMotionEvent event = new OnvifMotionEvent();
        String currentTag = null;
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                currentTag = getXpp().getName();
                String localName = currentTag.contains(":") ? currentTag.substring(currentTag.indexOf(":") + 1) : currentTag;

                // wsnt:Topic elem kezelése
                if ("Topic".equals(localName)) {
                    getXpp().next();
                    String topic = getXpp().getText();
                    if (topic != null) {
                        // Motion események felismerése
                        if (topic.contains("Motion") || topic.contains("MotionAlarm")) {
                            event.setEventType("MotionDetected");
                        } else if (topic.contains("MotionStopped")) {
                            event.setEventType("MotionStopped");
                        } else {
                            event.setEventType(topic); // teljes topic útvonal
                        }
                    }
                }
                // wsnt:Message -> tt:Message elem kezelése
                else if ("Message".equals(localName)) {
                    // UtcTime attribútum a tt:Message elemen
                    String utcTime = getXpp().getAttributeValue(null, "UtcTime");
                    if (utcTime != null) {
                        try {
                            // Többféle dátumformátum támogatása
                            SimpleDateFormat[] formats = {
                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
                                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
                                DATE_FORMAT
                            };
                            boolean parsed = false;
                            for (SimpleDateFormat format : formats) {
                                try {
                                    Date timestamp = format.parse(utcTime);
                                    event.setTimestamp(timestamp);
                                    parsed = true;
                                    break;
                                } catch (ParseException e) {
                                    // próbáljuk a következő formátumot
                                }
                            }
                            if (!parsed) {
                                event.setTimestamp(new Date());
                            }
                        } catch (Exception e) {
                            event.setTimestamp(new Date());
                        }
                    }
                    
                    // tt:Source és tt:Data elemek feldolgozása
                    parseMessageContent(event);
                }
                
            } else if (eventType == XmlPullParser.END_TAG) {
                String endTag = getXpp().getName();
                String endLocalName = endTag.contains(":") ? endTag.substring(endTag.indexOf(":") + 1) : endTag;
                if ("NotificationMessage".equals(endLocalName)) {
                    break;
                }
            }
            
            eventType = getXpp().next();
        }
        
        return event.getEventType() != null ? event : null;
    }
    
    private void parseMessageContent(OnvifMotionEvent event) throws XmlPullParserException, IOException {
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = getXpp().getName();
                String localName = tagName.contains(":") ? tagName.substring(tagName.indexOf(":") + 1) : tagName;
                
                // tt:Source elem kezelése
                if ("Source".equals(localName)) {
                    parseSource(event);
                }
                // tt:Data elem kezelése
                else if ("Data".equals(localName)) {
                    parseData(event);
                }
                // tt:SimpleItem elemek kezelése (Source és Data alatt)
                else if ("SimpleItem".equals(localName)) {
                    String name = getXpp().getAttributeValue(null, "Name");
                    String value = getXpp().getAttributeValue(null, "Value");
                    
                    if (value != null) {
                        // Source mezők
                        if ("VideoSourceConfigurationToken".equals(name) || 
                            "Source".equals(name)) {
                            event.setSourceToken(value);
                        }
                        // Data mezők
                        else if ("IsMotion".equals(name)) {
                            // IsMotion = true -> MotionDetected
                            if ("true".equalsIgnoreCase(value)) {
                                event.setEventType("MotionDetected");
                            } else {
                                event.setEventType("MotionStopped");
                            }
                        } else if ("State".equals(name)) {
                            // State = true -> MotionDetected
                            if ("true".equalsIgnoreCase(value)) {
                                event.setEventType("MotionDetected");
                            } else {
                                event.setEventType("MotionStopped");
                            }
                        } else if ("Confidence".equals(name)) {
                            try {
                                event.setConfidence(Float.parseFloat(value));
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                    }
                }
                
            } else if (eventType == XmlPullParser.END_TAG) {
                String endTag = getXpp().getName();
                String endLocalName = endTag.contains(":") ? endTag.substring(endTag.indexOf(":") + 1) : endTag;
                if ("Message".equals(endLocalName)) {
                    break;
                }
            }
            
            eventType = getXpp().next();
        }
    }
    
    private void parseSource(OnvifMotionEvent event) throws XmlPullParserException, IOException {
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = getXpp().getName();
                String localName = tagName.contains(":") ? tagName.substring(tagName.indexOf(":") + 1) : tagName;
                
                if ("SimpleItem".equals(localName)) {
                    String name = getXpp().getAttributeValue(null, "Name");
                    String value = getXpp().getAttributeValue(null, "Value");
                    
                    if (value != null && ("VideoSourceConfigurationToken".equals(name) || 
                                         "Source".equals(name))) {
                        event.setSourceToken(value);
                    }
                }
                
            } else if (eventType == XmlPullParser.END_TAG) {
                String endTag = getXpp().getName();
                String endLocalName = endTag.contains(":") ? endTag.substring(endTag.indexOf(":") + 1) : endTag;
                if ("Source".equals(endLocalName)) {
                    break;
                }
            }
            
            eventType = getXpp().next();
        }
    }
    
    private void parseData(OnvifMotionEvent event) throws XmlPullParserException, IOException {
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = getXpp().getName();
                String localName = tagName.contains(":") ? tagName.substring(tagName.indexOf(":") + 1) : tagName;
                
                if ("SimpleItem".equals(localName)) {
                    String name = getXpp().getAttributeValue(null, "Name");
                    String value = getXpp().getAttributeValue(null, "Value");
                    
                    if (value != null) {
                        if ("IsMotion".equals(name)) {
                            if ("true".equalsIgnoreCase(value)) {
                                event.setEventType("MotionDetected");
                            } else {
                                event.setEventType("MotionStopped");
                            }
                        } else if ("State".equals(name)) {
                            if ("true".equalsIgnoreCase(value)) {
                                event.setEventType("MotionDetected");
                            } else {
                                event.setEventType("MotionStopped");
                            }
                        } else if ("Confidence".equals(name)) {
                            try {
                                event.setConfidence(Float.parseFloat(value));
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                    }
                } else if ("Rectangle".equals(localName)) {
                    OnvifMotionDetectionArea area = parseRectangle();
                    event.setDetectionArea(area);
                }
                
            } else if (eventType == XmlPullParser.END_TAG) {
                String endTag = getXpp().getName();
                String endLocalName = endTag.contains(":") ? endTag.substring(endTag.indexOf(":") + 1) : endTag;
                if ("Data".equals(endLocalName)) {
                    break;
                }
            }
            
            eventType = getXpp().next();
        }
    }
    
    private OnvifMotionDetectionArea parseRectangle() throws XmlPullParserException, IOException {
        OnvifMotionDetectionArea area = new OnvifMotionDetectionArea();
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = getXpp().getName();
                getXpp().next();
                String value = getXpp().getText();
                
                switch (tagName) {
                    case "x":
                        area.setX(Float.parseFloat(value));
                        break;
                    case "y":
                        area.setY(Float.parseFloat(value));
                        break;
                    case "width":
                        area.setWidth(Float.parseFloat(value));
                        break;
                    case "height":
                        area.setHeight(Float.parseFloat(value));
                        break;
                }
                
            } else if (eventType == XmlPullParser.END_TAG && getXpp().getName().equals("Rectangle")) {
                break;
            }
            
            eventType = getXpp().next();
        }
        
        return area;
    }
}

