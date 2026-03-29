package be.teletask.onvif.upnp;

import be.teletask.onvif.models.UPnPDevice;
import be.teletask.onvif.parsers.UPnPParser;
import be.teletask.onvif.responses.OnvifResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class UPnPExecutor {

    //Constants
    public static final String TAG = UPnPExecutor.class.getSimpleName();

    //Attributes
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    /** volatile: {@link #clear()} és {@link CompletableFuture} callback között láthatóság */
    private volatile UPnPResponseListener responseListener;

    //Constructors

    UPnPExecutor(UPnPResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    //Methods

    /**
     * Sends a request to a UPnP device.
     */
    public void sendRequest(UPnPDevice device) {
        performXmlRequest(device, buildUPnPRequest(device));
    }

    /**
     * Sends a request to a UPnP device.
     */
    void getDeviceInformation(UPnPDevice device, UPnPDeviceInformationListener listener) {
        HttpRequest request = buildUPnPRequest(device);
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        future.whenComplete((xmlResponse, throwable) -> {
            if (throwable != null) {
                listener.onError(device, -1, throwable.getMessage());
                return;
            }
            if (xmlResponse.statusCode() == 200 && xmlResponse.body() != null) {
                UPnPDeviceInformation information = parseDeviceInformation(device, xmlResponse.body());
                device.setDeviceInformation(information);
                listener.onDeviceInformationReceived(device, information);
                return;
            }

            String errorMessage = xmlResponse.body() != null ? xmlResponse.body() : "";
            listener.onError(device, xmlResponse.statusCode(), errorMessage);
        });
    }

    /**
     * Elengedi a válaszfigyelőt. A {@link HttpClient} nem igényel explicit lezárást.
     * A már elindult {@link #sendAsync} hívások befejeződhetnek; ha addig {@code clear()} futott, a figyelő már nem hívódik meg.
     */
    public void clear() {
        responseListener = null;
    }

    //Properties

    public void setResponseListener(UPnPResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    private void performXmlRequest(UPnPDevice device, HttpRequest xmlRequest) {
        if (xmlRequest == null) {
            return;
        }

        CompletableFuture<HttpResponse<String>> future = client.sendAsync(xmlRequest, HttpResponse.BodyHandlers.ofString());
        future.whenComplete((xmlResponse, throwable) -> {
            UPnPResponseListener rl = responseListener;
            if (rl == null) {
                return;
            }
            if (throwable != null) {
                rl.onError(device, -1, throwable.getMessage());
                return;
            }
            if (xmlResponse.statusCode() == 200 && xmlResponse.body() != null) {
                parseResponse(device, xmlResponse.body());
                return;
            }

            String errorMessage = xmlResponse.body() != null ? xmlResponse.body() : "";
            rl.onError(device, xmlResponse.statusCode(), errorMessage);
        });
    }

    private UPnPDeviceInformation parseDeviceInformation(UPnPDevice device, String xmlBody) {
        return new UPnPParser().parse(new OnvifResponse(xmlBody));
    }

    private void parseResponse(UPnPDevice device, String xmlBody) {
        UPnPParser parser = new UPnPParser();
        parser.parse(new OnvifResponse(xmlBody));
    }

    private HttpRequest buildUPnPRequest(UPnPDevice device) {
        return HttpRequest.newBuilder(URI.create(device.getLocation()))
                .header("Content-Type", "text/xml; charset=utf-8")
                .GET()
                .timeout(Duration.ofSeconds(100))
                .build();
    }

}
