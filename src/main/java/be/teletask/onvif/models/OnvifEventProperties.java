package be.teletask.onvif.models;

import java.util.ArrayList;
import java.util.List;

/**
 * ONVIF EventProperties modell.
 * Tartalmazza a kamera által hirdetett esemény-topikokkal kapcsolatos
 * legfontosabb metaadatokat, valamint (ha elérhető) a motion esemény
 * topic útvonalát.
 */
public class OnvifEventProperties {

    public static final String TAG = OnvifEventProperties.class.getSimpleName();

    private String topicNamespaceLocation;
    private boolean fixedTopicSet;
    private final List<String> topicExpressionDialects = new ArrayList<>();
    private String messageContentFilterDialect;
    private String messageContentSchemaLocation;

    /**
     * Pl. "tns1:RuleEngine/CellMotionDetector/Motion"
     * ha a kamerán elérhető ilyen motion topic.
     */
    private String motionTopicPath;

    public String getTopicNamespaceLocation() {
        return topicNamespaceLocation;
    }

    public void setTopicNamespaceLocation(String topicNamespaceLocation) {
        this.topicNamespaceLocation = topicNamespaceLocation;
    }

    public boolean isFixedTopicSet() {
        return fixedTopicSet;
    }

    public void setFixedTopicSet(boolean fixedTopicSet) {
        this.fixedTopicSet = fixedTopicSet;
    }

    public List<String> getTopicExpressionDialects() {
        return topicExpressionDialects;
    }

    public void addTopicExpressionDialect(String dialect) {
        if (dialect != null && !dialect.isEmpty()) {
            topicExpressionDialects.add(dialect);
        }
    }

    public String getMessageContentFilterDialect() {
        return messageContentFilterDialect;
    }

    public void setMessageContentFilterDialect(String messageContentFilterDialect) {
        this.messageContentFilterDialect = messageContentFilterDialect;
    }

    public String getMessageContentSchemaLocation() {
        return messageContentSchemaLocation;
    }

    public void setMessageContentSchemaLocation(String messageContentSchemaLocation) {
        this.messageContentSchemaLocation = messageContentSchemaLocation;
    }

    public String getMotionTopicPath() {
        return motionTopicPath;
    }

    public void setMotionTopicPath(String motionTopicPath) {
        this.motionTopicPath = motionTopicPath;
    }

    @Override
    public String toString() {
        return "OnvifEventProperties{" +
                "topicNamespaceLocation='" + topicNamespaceLocation + '\'' +
                ", fixedTopicSet=" + fixedTopicSet +
                ", topicExpressionDialects=" + topicExpressionDialects +
                ", messageContentFilterDialect='" + messageContentFilterDialect + '\'' +
                ", messageContentSchemaLocation='" + messageContentSchemaLocation + '\'' +
                ", motionTopicPath='" + motionTopicPath + '\'' +
                '}';
    }
}


