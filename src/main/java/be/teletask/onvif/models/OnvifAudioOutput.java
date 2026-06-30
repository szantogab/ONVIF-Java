package be.teletask.onvif.models;

/**
 * ONVIF Media Service – fizikai audio kimenet (hangszóró).
 */
public class OnvifAudioOutput {

    private String token;

    public OnvifAudioOutput() {
    }

    public OnvifAudioOutput(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "OnvifAudioOutput{token='" + token + "'}";
    }
}
