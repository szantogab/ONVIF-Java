package be.teletask.onvif.parsers;

import be.teletask.onvif.models.OnvifEventProperties;
import be.teletask.onvif.responses.OnvifResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * ONVIF GetEventProperties válasz parser.
 *
 * A válaszból a legfontosabb metaadatokat olvassa ki, és ha elérhető,
 * megpróbálja felismerni a szabványos motion topic útvonalat:
 *   tns1:RuleEngine/CellMotionDetector/Motion
 */
public class GetEventPropertiesParser extends OnvifParser<OnvifEventProperties> {

    @Override
    public OnvifEventProperties parse(OnvifResponse response) {
        OnvifEventProperties props = new OnvifEventProperties();

        try {
            getXpp().setInput(new StringReader(response.getXml()));
            eventType = getXpp().getEventType();

            boolean inRuleEngine = false;
            boolean inCellMotionDetector = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = getXpp().getName();

                    if ("TopicNamespaceLocation".equals(name)) {
                        getXpp().next();
                        props.setTopicNamespaceLocation(getXpp().getText());
                    } else if ("FixedTopicSet".equals(name)) {
                        getXpp().next();
                        String text = getXpp().getText();
                        props.setFixedTopicSet(Boolean.parseBoolean(text));
                    } else if ("TopicExpressionDialect".equals(name)) {
                        getXpp().next();
                        props.addTopicExpressionDialect(getXpp().getText());
                    } else if ("MessageContentFilterDialect".equals(name)) {
                        getXpp().next();
                        props.setMessageContentFilterDialect(getXpp().getText());
                    } else if ("MessageContentSchemaLocation".equals(name)) {
                        getXpp().next();
                        props.setMessageContentSchemaLocation(getXpp().getText());
                    } else if ("RuleEngine".equals(name)) {
                        inRuleEngine = true;
                    } else if (inRuleEngine && "CellMotionDetector".equals(name)) {
                        inCellMotionDetector = true;
                    } else if (inCellMotionDetector && "Motion".equals(name)) {
                        // Standard ONVIF motion topic path
                        props.setMotionTopicPath("tns1:RuleEngine/CellMotionDetector/Motion");
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String name = getXpp().getName();
                    if ("RuleEngine".equals(name)) {
                        inRuleEngine = false;
                        inCellMotionDetector = false;
                    } else if ("CellMotionDetector".equals(name)) {
                        inCellMotionDetector = false;
                    }
                }

                eventType = getXpp().next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return props;
    }
}


