package be.teletask.onvif;

import be.teletask.onvif.listeners.OnvifResponseListener;
import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifServices;
import be.teletask.onvif.parsers.*;
import be.teletask.onvif.requests.OnvifRequest;
import be.teletask.onvif.responses.OnvifResponse;
import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
    private final MediaType reqBodyType = MediaType.parse("application/soap+xml; charset=utf-8;");
    private final ConcurrentHashMap<String, OkHttpClient> clientPool = new ConcurrentHashMap<>();
    private OnvifResponseListener onvifResponseListener;

    //Constructors

    OnvifExecutor(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;
    }

    /**
     * Visszaadja vagy létrehozza az OkHttpClient példányt az adott eszközhöz
     */
    private OkHttpClient getClientForDevice(OnvifDevice device) {
        String deviceKey = device.getHostName() + ":" + device.getUsername();
        return clientPool.computeIfAbsent(deviceKey, key -> {
            Credentials deviceCredentials = new Credentials(device.getUsername(), device.getPassword());
            DigestAuthenticator authenticator = new DigestAuthenticator(deviceCredentials);
            Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();

            return new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS) // PullMessages can keep the connection open longer, timeout will be applied per request
                    .addInterceptor(new AuthenticationCacheInterceptor(authCache))
                    .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                    .build();
        });
    }

    //Methods

    <T> void sendRequest(OnvifDevice device, OnvifRequest<T> request) {
        sendRequest(device, request, 5);
    }

    /**
     * Sends a request to the Onvif-compatible device.
     */
    <T> void sendRequest(OnvifDevice device, OnvifRequest<T> request, int timeoutSeconds) {
        Credentials deviceCredentials = new Credentials(device.getUsername(), device.getPassword());
        String body = OnvifXMLBuilder.getSoapHeader(deviceCredentials, request.getSoapHeader()) + request.getXml() + OnvifXMLBuilder.getEnvelopeEnd();
        LOGGER.log(Level.FINE, "Onvif Sending Request: {0}", body);
        performXmlRequest(device, request, buildOnvifRequest(device, request, RequestBody.create(body, reqBodyType)), timeoutSeconds);
    }

    /**
     * Clears up the resources.
     */
    void clear() {
        onvifResponseListener = null;
        // Kliensek leállítása és erőforrások felszabadítása
        for (OkHttpClient client : clientPool.values()) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
        clientPool.clear();
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
        Request request = new Request.Builder()
                .url(snapshotUri)
                .get()
                .build();

        OkHttpClient deviceClient = getClientForDevice(device);
        final Call call = deviceClient.newCall(request);
        call.timeout().timeout(timeoutSeconds, TimeUnit.SECONDS);

        try (Response response = call.execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                byte[] imageData = body.bytes();
                if (listener != null) {
                    listener.onSuccess(device, imageData);
                }
            } else {
                String errorMessage = body != null ? body.string() : "HTTP " + response.code();
                if (listener != null) {
                    listener.onError(new OnvifRequest.OnvifException(device, response.code(), errorMessage));
                }
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onError(new OnvifRequest.OnvifException(device, -1, e.getMessage()));
            }
        }
    }

    private <T> void performXmlRequest(OnvifDevice device, OnvifRequest<T> request, Request xmlRequest, int timeoutSeconds) {
        if (xmlRequest == null) return;

        OkHttpClient deviceClient = getClientForDevice(device);
        final Call call = deviceClient.newCall(xmlRequest);
        call.timeout().timeout(timeoutSeconds, TimeUnit.SECONDS);

        try (Response xmlResponse = call.execute()) {
            OnvifResponse<T> response = new OnvifResponse<>(request);
            ResponseBody xmlBody = xmlResponse.body();

            if (xmlResponse.code() == 200 && xmlBody != null) {
                response.setSuccess(true);
                response.setXml(xmlBody.string());
                parseResponse(device, response);
                return;
            }

            String errorMessage = "";
            if (xmlBody != null)
                errorMessage = xmlBody.string();

            if (request.getListener() != null)
                request.getListener().onError(new OnvifRequest.OnvifException(device, xmlResponse.code(), errorMessage));
            if (onvifResponseListener != null)
                onvifResponseListener.onError(new OnvifRequest.OnvifException(device, xmlResponse.code(), errorMessage));
        } catch (Exception e) {
            if (request.getListener() != null)
                request.getListener().onError(new OnvifRequest.OnvifException(device, -1, e.getMessage()));
            if (onvifResponseListener != null)
                onvifResponseListener.onError(new OnvifRequest.OnvifException(device, -1, e.getMessage()));
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
                onvifResponseListener.onResponse(device, response);
                break;
        }

        response.request().getListener().onSuccess(device, data);
    }

    private Request buildOnvifRequest(OnvifDevice device, OnvifRequest<?> request, RequestBody reqBody) {
        return new Request.Builder()
                .url(getUrlForRequest(device, request))
                .addHeader("Content-Type", "application/soap+xml; charset=utf-8")
                .post(reqBody)
                .build();
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
