package be.teletask.onvif;

import be.teletask.onvif.listeners.OnvifResponseListener;
import be.teletask.onvif.models.*;
import be.teletask.onvif.requests.*;
import be.teletask.onvif.responses.OnvifResponse;

import java.util.List;

/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class OnvifManager implements OnvifResponseListener {

    //Constants
    public final static String TAG = OnvifManager.class.getSimpleName();

    //Attributes
    private OnvifExecutor executor;
    private OnvifResponseListener onvifResponseListener;

    //Constructors
    public OnvifManager() {
        this(null);
    }

    private OnvifManager(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;
        executor = new OnvifExecutor(this);
    }

    //Methods
    public void getServices(OnvifDevice device, OnvifRequest.Listener<OnvifServices> listener) {
        final GetServicesRequest request = new GetServicesRequest(listener);
        executor.sendRequest(device, request);
    }

    public void getDeviceInformation(OnvifDevice device, OnvifRequest.Listener<OnvifDeviceInformation> listener) {
        final GetDeviceInformationRequest request = new GetDeviceInformationRequest(listener);
        executor.sendRequest(device, request);
    }

    public void getMediaProfiles(OnvifDevice device, OnvifRequest.Listener<List<OnvifMediaProfile>> listener) {
        final GetMediaProfilesRequest request = new GetMediaProfilesRequest(listener);
        executor.sendRequest(device, request);
    }

    public void getMediaStreamURI(OnvifDevice device, OnvifMediaProfile profile, OnvifRequest.Listener<String> listener) {
        final GetMediaStreamRequest request = new GetMediaStreamRequest(profile, listener);
        executor.sendRequest(device, request);
    }

    public void getMediaSnapshotURI(OnvifDevice device, OnvifMediaProfile profile, OnvifRequest.Listener<String> listener) {
        final GetSnapshotUriRequest request = new GetSnapshotUriRequest(profile, listener);
        executor.sendRequest(device, request);
    }

    public void ptzContinuousMove(OnvifDevice device, String profileToken, Double velocityX, Double velocityY, Double velocityZ, Integer timeout, OnvifRequest.Listener<Void> listener) {
        final ContinuousMoveRequest request = new ContinuousMoveRequest(profileToken, timeout, velocityX, velocityY, velocityZ, listener);
        executor.sendRequest(device, request);
    }

    public void ptzRelativeMove(OnvifDevice device, String profileToken, Double translationX, Double translationY, Double zoom, OnvifRequest.Listener<Void> listener) {
        final RelativeMoveRequest request = new RelativeMoveRequest(profileToken, translationX, translationY, zoom, listener);
        executor.sendRequest(device, request);
    }

    public void ptzAbsoluteMove(OnvifDevice device, String profileToken, Double positionX, Double positionY, Double zoom, OnvifRequest.Listener<Void> listener) {
        final AbsoluteMoveRequest request = new AbsoluteMoveRequest(profileToken, positionX, positionY, zoom, listener);
        executor.sendRequest(device, request);
    }

    public void ptzStop(OnvifDevice device, String profileToken, boolean panTilt, boolean zoom, OnvifRequest.Listener<Void> listener) {
        final StopRequest request = new StopRequest(profileToken, panTilt, zoom, listener);
        executor.sendRequest(device, request);
    }

    public void sendOnvifRequest(OnvifDevice device, OnvifRequest<?> request) {
        executor.sendRequest(device, request);
    }

    // Motion Detection & Events

    // Motion Detection Methods

    /**
     * Lekérdezi a mozgásérzékelési konfigurációt
     * @param device ONVIF eszköz
     * @param listener válasz listener
     */
    public void getMotionDetectionConfiguration(OnvifDevice device, OnvifRequest.Listener<OnvifMotionDetection> listener) {
        final GetMotionDetectionConfigurationRequest request = new GetMotionDetectionConfigurationRequest(listener);
        executor.sendRequest(device, request);
    }

    /**
     * Beállítja a mozgásérzékelési konfigurációt
     * @param device ONVIF eszköz
     * @param motionDetection mozgásérzékelési konfiguráció
     * @param listener válasz listener
     */
    public void setMotionDetectionConfiguration(OnvifDevice device, OnvifMotionDetection motionDetection, OnvifRequest.Listener<Void> listener) {
        final SetMotionDetectionConfigurationRequest request = new SetMotionDetectionConfigurationRequest(motionDetection, listener);
        executor.sendRequest(device, request);
    }

    /**
     * Lekérdezi az elérhető analytics engine-eket
     * @param device ONVIF eszköz
     * @param listener válasz listener
     */
    public void getAnalyticsEngines(OnvifDevice device, OnvifRequest.Listener<List<String>> listener) {
        final GetAnalyticsEnginesRequest request = new GetAnalyticsEnginesRequest(listener);
        executor.sendRequest(device, request);
    }

    /**
     * Létrehoz egy pull point subscription-t mozgásérzékelési eseményekhez
     * @param device ONVIF eszköz
     * @param listener válasz listener
     */
    public void createPullPointSubscription(OnvifDevice device, String[] eventFilters, int initialTerminationTimeSeconds,  OnvifRequest.Listener<String> listener) {
        final CreatePullPointSubscriptionRequest request = new CreatePullPointSubscriptionRequest(listener, eventFilters, initialTerminationTimeSeconds);
        executor.sendRequest(device, request);
    }

    /**
     * Lekérdezi a mozgásérzékelési eseményeket
     * @param device ONVIF eszköz
     * @param subscriptionReference feliratkozás referenciája
     * @param listener válasz listener
     */
    public void pullMessages(OnvifDevice device, String subscriptionReference, int messageLimit, int timeoutSeconds, OnvifRequest.Listener<List<OnvifMotionEvent>> listener) {
        final PullMessagesRequest request = new PullMessagesRequest(subscriptionReference, messageLimit, timeoutSeconds, listener);
        executor.sendRequest(device, request, timeoutSeconds);
    }

    /**
     * Lekérdezi a kamera által támogatott esemény-topikokat és EventProperties-t.
     * A válasz egy OnvifEventProperties modellben érkezik vissza.
     *
     * @param device   ONVIF eszköz
     * @param listener válasz listener
     */
    public void getEventProperties(OnvifDevice device, OnvifRequest.Listener<OnvifEventProperties> listener) {
        final GetEventPropertiesRequest request = new GetEventPropertiesRequest(listener);
        executor.sendRequest(device, request);
    }

    // Event Broker Methods

    /**
     * Hozzáad egy Event Broker-t a kamerához
     * @param device ONVIF eszköz
     * @param brokerAddress Event Broker címe (URL)
     * @param listener válasz listener
     */
    public void addEventBroker(OnvifDevice device, String brokerAddress, OnvifRequest.Listener<Void> listener) {
        final AddEventBrokerRequest request = new AddEventBrokerRequest(brokerAddress, listener);
        executor.sendRequest(device, request);
    }

    /**
     * Töröl egy Event Broker-t a kameráról
     * @param device ONVIF eszköz
     * @param brokerToken Event Broker token-je
     * @param listener válasz listener
     */
    public void deleteEventBroker(OnvifDevice device, String brokerToken, OnvifRequest.Listener<Void> listener) {
        final DeleteEventBrokerRequest request = new DeleteEventBrokerRequest(brokerToken, listener);
        executor.sendRequest(device, request);
    }

    /**
     * Lekérdezi az Event Broker-eket
     * @param device ONVIF eszköz
     * @param listener válasz listener
     */
    public void getEventBrokers(OnvifDevice device, OnvifRequest.Listener<List<String>> listener) {
        final GetEventBrokersRequest request = new GetEventBrokersRequest(listener);
        executor.sendRequest(device, request);
    }

    /**
     * Leiratkozás egy meglévő PullPoint / SubscriptionManager feliratkozásról.
     *
     * @param device                ONVIF eszköz (hitelesítéshez)
     * @param subscriptionReference A CreatePullPointSubscription válaszában kapott Address URL
     * @param listener              válasz listener
     */
    public void unsubscribe(OnvifDevice device, String subscriptionReference, OnvifRequest.Listener<Void> listener) {
        final UnsubscribeRequest request = new UnsubscribeRequest(subscriptionReference, listener);
        executor.sendRequest(device, request);
    }

    public void setOnvifResponseListener(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;
    }

    /**
     * Clear up the resources.
     */
    public void destroy() {
        onvifResponseListener = null;
        executor.clear();
    }

    @Override
    public void onResponse(OnvifDevice onvifDevice, OnvifResponse response) {
        if (onvifResponseListener != null)
            onvifResponseListener.onResponse(onvifDevice, response);
    }

    @Override
    public void onError(OnvifRequest.OnvifException exception) {
        if (onvifResponseListener != null)
            onvifResponseListener.onError(exception);
    }
}
