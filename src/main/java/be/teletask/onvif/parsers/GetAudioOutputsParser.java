package be.teletask.onvif.parsers;

import be.teletask.onvif.models.OnvifAudioOutput;
import be.teletask.onvif.responses.OnvifResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class GetAudioOutputsParser extends OnvifParser<List<OnvifAudioOutput>> {

    public static final String TAG = GetAudioOutputsParser.class.getSimpleName();

    @Override
    public List<OnvifAudioOutput> parse(OnvifResponse response) {
        List<OnvifAudioOutput> audioOutputs = new ArrayList<>();

        try {
            getXpp().setInput(new StringReader(response.getXml()));
            eventType = getXpp().getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "AudioOutputs".equals(getLocalName(getXpp().getName()))) {
                    String token = getXpp().getAttributeValue(null, "token");
                    if (token != null && !token.isEmpty()) {
                        audioOutputs.add(new OnvifAudioOutput(token));
                    }
                }
                eventType = getXpp().next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return audioOutputs;
    }

    private String getLocalName(String tagName) {
        if (tagName == null) return "";
        int colonIndex = tagName.indexOf(':');
        return colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
    }
}
