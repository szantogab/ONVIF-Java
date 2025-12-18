package be.teletask.onvif.parsers;

import be.teletask.onvif.models.OnvifMediaProfile;
import be.teletask.onvif.responses.OnvifResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomas Verhelst on 04/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class GetMediaProfilesParser extends OnvifParser<List<OnvifMediaProfile>> {

    //Constants
    public static final String TAG = GetMediaProfilesParser.class.getSimpleName();

    @Override
    public List<OnvifMediaProfile> parse(OnvifResponse response) {
        List<OnvifMediaProfile> profiles = new ArrayList<>();

        try {
            getXpp().setInput(new StringReader(response.getXml()));
            eventType = getXpp().getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && getLocalName(getXpp().getName()).equals("Profiles")) {
                    OnvifMediaProfile profile = parseProfile();
                    if (profile != null) {
                        profiles.add(profile);
                    }
                }
                eventType = getXpp().next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return profiles;
    }

    private OnvifMediaProfile parseProfile() throws XmlPullParserException, IOException {
        OnvifMediaProfile profile = new OnvifMediaProfile();

        // Parse attributes from Profiles element
        profile.setToken(getXpp().getAttributeValue(null, "token"));
        String fixedAttr = getXpp().getAttributeValue(null, "fixed");
        profile.setFixed("true".equalsIgnoreCase(fixedAttr));

        // Track current context for nested elements
        String currentSection = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            eventType = getXpp().next();

            if (eventType == XmlPullParser.START_TAG) {
                String localName = getLocalName(getXpp().getName());

                switch (localName) {
                    case "Name":
                        getXpp().next();
                        String nameText = getXpp().getText();
                        if (nameText != null) {
                            if ("VideoSourceConfiguration".equals(currentSection)) {
                                profile.setVideoSourceConfigName(nameText);
                            } else if ("VideoEncoderConfiguration".equals(currentSection)) {
                                profile.setVideoEncoderConfigName(nameText);
                            } else if ("PTZConfiguration".equals(currentSection)) {
                                profile.setPtzConfigName(nameText);
                            } else {
                                profile.setName(nameText);
                            }
                        }
                        break;

                    case "VideoSourceConfiguration":
                        currentSection = "VideoSourceConfiguration";
                        profile.setVideoSourceConfigToken(getXpp().getAttributeValue(null, "token"));
                        break;

                    case "VideoEncoderConfiguration":
                        currentSection = "VideoEncoderConfiguration";
                        profile.setVideoEncoderConfigToken(getXpp().getAttributeValue(null, "token"));
                        break;

                    case "PTZConfiguration":
                        currentSection = "PTZConfiguration";
                        profile.setPtzConfigToken(getXpp().getAttributeValue(null, "token"));
                        break;

                    case "SourceToken":
                        getXpp().next();
                        String sourceToken = getXpp().getText();
                        if (sourceToken != null) {
                            profile.setVideoSourceToken(sourceToken);
                        }
                        break;

                    case "Bounds":
                        profile.setBoundsX(parseIntAttribute("x"));
                        profile.setBoundsY(parseIntAttribute("y"));
                        profile.setBoundsWidth(parseIntAttribute("width"));
                        profile.setBoundsHeight(parseIntAttribute("height"));
                        break;

                    case "Encoding":
                        getXpp().next();
                        String encoding = getXpp().getText();
                        if (encoding != null) {
                            profile.setEncoding(encoding);
                        }
                        break;

                    case "Resolution":
                        parseResolution(profile);
                        break;

                    case "Quality":
                        getXpp().next();
                        String quality = getXpp().getText();
                        if (quality != null) {
                            try {
                                profile.setQuality((int) Float.parseFloat(quality));
                            } catch (NumberFormatException ignored) {}
                        }
                        break;

                    case "RateControl":
                        parseRateControl(profile);
                        break;

                    case "H264":
                        parseH264(profile);
                        break;

                    case "NodeToken":
                        getXpp().next();
                        String nodeToken = getXpp().getText();
                        if (nodeToken != null) {
                            profile.setPtzNodeToken(nodeToken);
                        }
                        break;
                }

            } else if (eventType == XmlPullParser.END_TAG) {
                String localName = getLocalName(getXpp().getName());

                // Clear section context when leaving configuration elements
                if ("VideoSourceConfiguration".equals(localName) ||
                    "VideoEncoderConfiguration".equals(localName) ||
                    "PTZConfiguration".equals(localName)) {
                    currentSection = null;
                }

                // Profile element ended, return the parsed profile
                if ("Profiles".equals(localName)) {
                    return profile;
                }
            }
        }

        return profile;
    }

    private void parseResolution(OnvifMediaProfile profile) throws XmlPullParserException, IOException {
        while (eventType != XmlPullParser.END_DOCUMENT) {
            eventType = getXpp().next();

            if (eventType == XmlPullParser.START_TAG) {
                String localName = getLocalName(getXpp().getName());

                if ("Width".equals(localName)) {
                    getXpp().next();
                    String width = getXpp().getText();
                    if (width != null) {
                        try {
                            profile.setWidth(Integer.parseInt(width));
                        } catch (NumberFormatException ignored) {}
                    }
                } else if ("Height".equals(localName)) {
                    getXpp().next();
                    String height = getXpp().getText();
                    if (height != null) {
                        try {
                            profile.setHeight(Integer.parseInt(height));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG && getLocalName(getXpp().getName()).equals("Resolution")) {
                break;
            }
        }
    }

    private void parseRateControl(OnvifMediaProfile profile) throws XmlPullParserException, IOException {
        while (eventType != XmlPullParser.END_DOCUMENT) {
            eventType = getXpp().next();

            if (eventType == XmlPullParser.START_TAG) {
                String localName = getLocalName(getXpp().getName());

                if ("FrameRateLimit".equals(localName)) {
                    getXpp().next();
                    String frameRate = getXpp().getText();
                    if (frameRate != null) {
                        try {
                            profile.setFrameRateLimit(Integer.parseInt(frameRate));
                        } catch (NumberFormatException ignored) {}
                    }
                } else if ("EncodingInterval".equals(localName)) {
                    getXpp().next();
                    String interval = getXpp().getText();
                    if (interval != null) {
                        try {
                            profile.setEncodingInterval(Integer.parseInt(interval));
                        } catch (NumberFormatException ignored) {}
                    }
                } else if ("BitrateLimit".equals(localName)) {
                    getXpp().next();
                    String bitrate = getXpp().getText();
                    if (bitrate != null) {
                        try {
                            profile.setBitrateLimit(Integer.parseInt(bitrate));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG && getLocalName(getXpp().getName()).equals("RateControl")) {
                break;
            }
        }
    }

    private void parseH264(OnvifMediaProfile profile) throws XmlPullParserException, IOException {
        while (eventType != XmlPullParser.END_DOCUMENT) {
            eventType = getXpp().next();

            if (eventType == XmlPullParser.START_TAG) {
                String localName = getLocalName(getXpp().getName());

                if ("GovLength".equals(localName)) {
                    getXpp().next();
                    String govLength = getXpp().getText();
                    if (govLength != null) {
                        try {
                            profile.setGovLength(Integer.parseInt(govLength));
                        } catch (NumberFormatException ignored) {}
                    }
                } else if ("H264Profile".equals(localName)) {
                    getXpp().next();
                    String h264Profile = getXpp().getText();
                    if (h264Profile != null) {
                        profile.setH264Profile(h264Profile);
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG && getLocalName(getXpp().getName()).equals("H264")) {
                break;
            }
        }
    }

    private int parseIntAttribute(String attrName) {
        String value = getXpp().getAttributeValue(null, attrName);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private String getLocalName(String tagName) {
        if (tagName == null) return "";
        int colonIndex = tagName.indexOf(':');
        return colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
    }
}
