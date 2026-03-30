package be.teletask.onvif;

import be.teletask.onvif.http.HttpDigest;
import be.teletask.onvif.listeners.OnvifResponseListener;
import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifServices;
import be.teletask.onvif.parsers.*;
import be.teletask.onvif.requests.OnvifRequest;
import be.teletask.onvif.responses.OnvifResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class OnvifExecutor {

    //Constants
    public static final String TAG = OnvifExecutor.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(OnvifExecutor.class.getName());

    //Attributes
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    /** volatile: {@link #clear()} és párhuzamos HTTP-válasz feldolgozás között láthatóság */
    private volatile OnvifResponseListener onvifResponseListener;

    //Constructors

    OnvifExecutor(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;
    }

    //Methods

    <T> void sendRequest(OnvifDevice device, OnvifRequest<T> request) {
        sendRequest(device, request, 10);
    }

    /**
     * Sends a request to the Onvif-compatible device.
     */
    <T> void sendRequest(OnvifDevice device, OnvifRequest<T> request, int timeoutSeconds) {
        String body = OnvifXMLBuilder.getSoapHeader(device.getUsername(), device.getPassword(), request.getSoapHeader())
                + request.getXml() + OnvifXMLBuilder.getEnvelopeEnd();
        LOGGER.log(Level.FINE, "Onvif Sending Request: {0}", body);
        performXmlRequest(device, request, body, timeoutSeconds);
    }

    /**
     * Elengedi a globális válaszfigyelőt. A {@link HttpClient}-nek nincs {@code close()} API-ja;
     * a belső connection pool a JVM életciklusáig él, nincs külön explicit lezárás.
     * A már futó szinkron HTTP hívások a visszatéréskor még hívhatnak listener-t;
     * a {@code null} figyelő miatt a globális callback-ek elmaradnak (NPE elkerülése).
     */
    void clear() {
        onvifResponseListener = null;
    }

    //Properties

    public void setOnvifResponseListener(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;
    }

    /**
     * Letölti a snapshot képet a megadott URI-ról
     * @param device ONVIF eszköz (hitelesítéshez)
     * @param snapshotUri Snapshot URI
     * @param listener Válasz listener (ByteArray)
     */
    void downloadSnapshot(OnvifDevice device, String snapshotUri, int timeoutSeconds, OnvifRequest.Listener<byte[]> listener) {
        try {
            URI uri = URI.create(snapshotUri);
            Map<String, String> headers = new HashMap<>();
            HttpResponse<byte[]> response = HttpDigest.executeBytes(
                    httpClient,
                    "GET",
                    uri,
                    headers,
                    null,
                    device.getUsername(),
                    device.getPassword(),
                    Duration.ofSeconds(timeoutSeconds)
            );

            if (response.statusCode() == 200) {
                byte[] imageData = response.body();
                if (listener != null) {
                    listener.onSuccess(device, imageData);
                }
            } else {
                String errorMessage = response.body() != null && response.body().length > 0
                        ? new String(response.body(), java.nio.charset.StandardCharsets.UTF_8)
                        : "HTTP " + response.statusCode();
                if (listener != null) {
                    listener.onError(new OnvifRequest.OnvifException(device, response.statusCode(), errorMessage));
                }
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(new OnvifRequest.OnvifException(device, -1, e.getMessage()));
            }
        }
    }

    private <T> void performXmlRequest(OnvifDevice device, OnvifRequest<T> request, String body, int timeoutSeconds) {
        if (body == null) {
            return;
        }

        try {
            URI uri = URI.create(getUrlForRequest(device, request));
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/soap+xml; charset=utf-8");

            HttpResponse<String> xmlResponse = HttpDigest.execute(
                    httpClient,
                    "POST",
                    uri,
                    headers,
                    body,
                    device.getUsername(),
                    device.getPassword(),
                    Duration.ofSeconds(timeoutSeconds)
            );

            OnvifResponse<T> response = new OnvifResponse<>(request);

            if (xmlResponse.statusCode() == 200 && xmlResponse.body() != null) {
                response.setSuccess(true);
                response.setXml(xmlResponse.body());
                parseResponse(device, response);
                return;
            }

            String errorMessage = xmlResponse.body() != null ? xmlResponse.body() : "";

            if (request.getListener() != null) {
                request.getListener().onError(new OnvifRequest.OnvifException(device, xmlResponse.statusCode(), errorMessage));
            }
            if (onvifResponseListener != null) {
                onvifResponseListener.onError(new OnvifRequest.OnvifException(device, xmlResponse.statusCode(), errorMessage));
            }
        } catch (Exception e) {
            if (request.getListener() != null) {
                request.getListener().onError(new OnvifRequest.OnvifException(device, -1, e.getMessage()));
            }
            if (onvifResponseListener != null) {
                onvifResponseListener.onError(new OnvifRequest.OnvifException(device, -1, e.getMessage()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void parseResponse(OnvifDevice device, OnvifResponse<T> response) {
        T data = null;
        switch (response.request().getType()) {
            case GET_SERVICES:
                OnvifServices path = new GetServicesParser().parse(response);
                device.setPath(path);
                data = (T) path;
                break;
            case GET_DEVICE_INFORMATION:
                data = (T) new GetDeviceInformationParser().parse(response);
                break;
            case GET_MEDIA_PROFILES:
                data = (T) new GetMediaProfilesParser().parse(response);
                break;
            case GET_STREAM_URI:
            case GET_SNAPSHOT_URI:
                data = (T) new GetMediaStreamParser().parse(response);
                break;
            case GET_MOTION_DETECTION_CONFIGURATION:
                data = (T) new GetMotionDetectionConfigurationParser().parse(response);
                break;
            case SET_MOTION_DETECTION_CONFIGURATION:
            case UNSUBSCRIBE:
            case ADD_EVENT_BROKER:
            case DELETE_EVENT_BROKER:
                break;
            case GET_ANALYTICS_ENGINES:
                data = (T) new GetAnalyticsEnginesParser().parse(response);
                break;
            case CREATE_PULL_POINT_SUBSCRIPTION:
                data = (T) new CreatePullPointSubscriptionParser().parse(response);
                break;
            case PULL_MESSAGES:
                data = (T) new PullMessagesParser().parse(response);
                break;
            case GET_EVENT_PROPERTIES:
                data = (T) new GetEventPropertiesParser().parse(response);
                break;
            case GET_EVENT_BROKERS:
                data = (T) new GetEventBrokersParser().parse(response);
                break;
            default:
                if (onvifResponseListener != null) {
                    onvifResponseListener.onResponse(device, response);
                }
                break;
        }

        response.request().getListener().onSuccess(device, data);
    }

    private String getUrlForRequest(OnvifDevice device, OnvifRequest<?> request) {
        // A PULL_MESSAGES és UNSUBSCRIBE kéréseknél az ONVIF szabvány szerint
        // a PullPoint / SubscriptionManager "Address" mezője tartalmazza a teljes
        // címet, ahová a hívást küldeni kell. Ilyenkor NEM a device host +
        // eventsPath kombinációt kell használni, hanem közvetlenül ezt az URL-t.
        if (request.getType() == be.teletask.onvif.models.OnvifType.PULL_MESSAGES
                && request instanceof be.teletask.onvif.requests.PullMessagesRequest) {
            return ((be.teletask.onvif.requests.PullMessagesRequest) request).getSubscriptionReference();
        }

        if (request.getType() == be.teletask.onvif.models.OnvifType.UNSUBSCRIBE
                && request instanceof be.teletask.onvif.requests.UnsubscribeRequest) {
            return ((be.teletask.onvif.requests.UnsubscribeRequest) request).getSubscriptionReference();
        }

        return device.getHostName() + getPathForRequest(device, request);
    }

    private String getPathForRequest(OnvifDevice device, OnvifRequest<?> request) {
        switch (request.getType()) {
            case GET_SERVICES:
                return device.getPath().getServicesPath();
            case GET_DEVICE_INFORMATION:
                return device.getPath().getDeviceInformationPath();
            case GET_MEDIA_PROFILES:
                return device.getPath().getProfilesPath();
            case GET_STREAM_URI:
                return device.getPath().getStreamURIPath();
            case GET_MOTION_DETECTION_CONFIGURATION:
            case SET_MOTION_DETECTION_CONFIGURATION:
            case GET_ANALYTICS_ENGINES:
            case CREATE_ANALYTICS_ENGINE_CONTROL:
            case DELETE_ANALYTICS_ENGINE_CONTROL:
                return device.getPath().getAnalyticsPath();
            case GET_EVENT_PROPERTIES:
            case CREATE_PULL_POINT_SUBSCRIPTION:
            case PULL_MESSAGES:
            case ADD_EVENT_BROKER:
            case DELETE_EVENT_BROKER:
            case GET_EVENT_BROKERS:
                return device.getPath().getEventsPath();
        }

        return device.getPath().getServicesPath();
    }
}
