package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifAudioOutput;
import be.teletask.onvif.models.OnvifType;

import java.util.List;

public class GetAudioOutputsRequest implements OnvifRequest<List<OnvifAudioOutput>> {

    public static final String TAG = GetAudioOutputsRequest.class.getSimpleName();

    private final Listener<List<OnvifAudioOutput>> listener;

    public GetAudioOutputsRequest(Listener<List<OnvifAudioOutput>> listener) {
        this.listener = listener;
    }

    public Listener<List<OnvifAudioOutput>> getListener() {
        return listener;
    }

    @Override
    public String getXml() {
        return "<GetAudioOutputs xmlns=\"http://www.onvif.org/ver10/media/wsdl\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_AUDIO_OUTPUTS;
    }
}
