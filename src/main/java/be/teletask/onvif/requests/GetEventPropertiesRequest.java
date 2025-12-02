package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifEventProperties;
import be.teletask.onvif.models.OnvifType;

/**
 * ONVIF GetEventProperties kérés
 * A kamera által támogatott esemény-topikok és képességek lekérdezésére.
 */
public class GetEventPropertiesRequest implements OnvifRequest<OnvifEventProperties> {

    //Constants
    public static final String TAG = GetEventPropertiesRequest.class.getSimpleName();

    //Attributes
    private final Listener<OnvifEventProperties> listener;

    //Constructors
    public GetEventPropertiesRequest(Listener<OnvifEventProperties> listener) {
        super();
        this.listener = listener;
    }

    //Properties
    @Override
    public Listener<OnvifEventProperties> getListener() {
        return listener;
    }

    @Override
    public String getXml() {
        // ONVIF Events szolgáltatás szabványos GetEventProperties hívása
        return "<GetEventProperties xmlns=\"http://www.onvif.org/ver10/events/wsdl\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_EVENT_PROPERTIES;
    }
}


