package be.teletask.onvif.examples;

import be.teletask.onvif.OnvifManager;
import be.teletask.onvif.listeners.OnvifMotionDetectionListener;
import be.teletask.onvif.listeners.OnvifMotionEventListener;
import be.teletask.onvif.models.OnvifDevice;
import be.teletask.onvif.models.OnvifMotionDetection;
import be.teletask.onvif.models.OnvifMotionDetectionConfig;
import be.teletask.onvif.models.OnvifMotionDetectionArea;
import be.teletask.onvif.models.OnvifMotionEvent;
import be.teletask.onvif.requests.OnvifRequest;

import java.util.List;

/**
 * ONVIF mozgásérzékelési funkciók használatának példája
 * Created for motion detection capabilities
 * Copyright (c) 2024 TELETASK BVBA. All rights reserved.
 */
public class MotionDetectionExample implements OnvifMotionDetectionListener, OnvifMotionEventListener {

    private OnvifManager onvifManager;
    private OnvifDevice device;
    private String subscriptionReference;

    public MotionDetectionExample() {
        this.onvifManager = new OnvifManager();
    }

    /**
     * Példa a mozgásérzékelési konfiguráció lekérdezésére
     */
    public void getMotionDetectionConfiguration() {
        onvifManager.getMotionDetectionConfiguration(device, new OnvifRequest.Listener<OnvifMotionDetection>() {
            @Override
            public void onSuccess(OnvifDevice device, OnvifMotionDetection motionDetection) {
                System.out.println("Mozgásérzékelési konfiguráció lekérdezve:");
                System.out.println("Token: " + motionDetection.getToken());
                System.out.println("Név: " + motionDetection.getName());
                System.out.println("Engedélyezve: " + motionDetection.isEnabled());
                System.out.println("Érzékenység: " + motionDetection.getConfig().getSensitivity());
                System.out.println("Küszöbérték: " + motionDetection.getConfig().getThreshold());
            }

            @Override
            public void onError(OnvifRequest.OnvifException exception) {
                System.err.println("Hiba a mozgásérzékelési konfiguráció lekérdezése során: " + exception.getMessage());
            }
        });
    }

    /**
     * Példa a mozgásérzékelési konfiguráció beállítására
     */
    public void setMotionDetectionConfiguration() {
        // Mozgásérzékelési terület létrehozása (teljes kép)
        OnvifMotionDetectionArea detectionArea = new OnvifMotionDetectionArea(0.0f, 0.0f, 1.0f, 1.0f);
        
        // Konfiguráció létrehozása
        OnvifMotionDetectionConfig config = new OnvifMotionDetectionConfig();
        config.setSensitivity(75); // 75% érzékenység
        config.setThreshold(60);   // 60% küszöbérték
        config.setDetectionArea(detectionArea);
        config.setAutoMode(true);
        config.setMinDetectionTime(200);  // 200ms minimum
        config.setMaxDetectionTime(3000); // 3s maximum

        // Mozgásérzékelési objektum létrehozása
        OnvifMotionDetection motionDetection = new OnvifMotionDetection();
        motionDetection.setToken("MotionDetectionConfig");
        motionDetection.setName("Alapértelmezett mozgásérzékelés");
        motionDetection.setEnabled(true);
        motionDetection.setSourceToken("VideoSource_1");
        motionDetection.setConfig(config);

        onvifManager.setMotionDetectionConfiguration(device, motionDetection, new OnvifRequest.Listener<Void>() {
            @Override
            public void onSuccess(OnvifDevice device, Void result) {
                System.out.println("Mozgásérzékelési konfiguráció sikeresen beállítva!");
            }

            @Override
            public void onError(OnvifRequest.OnvifException exception) {
                System.err.println("Hiba a mozgásérzékelési konfiguráció beállítása során: " + exception.getMessage());
            }
        });
    }

    /**
     * Példa az analytics engine-ek lekérdezésére
     */
    public void getAnalyticsEngines() {
        onvifManager.getAnalyticsEngines(device, new OnvifRequest.Listener<List<String>>() {
            @Override
            public void onSuccess(OnvifDevice device, List<String> analyticsEngines) {
                System.out.println("Elérhető analytics engine-ek:");
                for (String engine : analyticsEngines) {
                    System.out.println("- " + engine);
                }
            }

            @Override
            public void onError(OnvifRequest.OnvifException exception) {
                System.err.println("Hiba az analytics engine-ek lekérdezése során: " + exception.getMessage());
            }
        });
    }

    /**
     * Példa a mozgásérzékelési esemény feliratkozás létrehozására
     */
    public void createMotionDetectionSubscription() {
        onvifManager.createPullPointSubscription(device, null, 60, new OnvifRequest.Listener<String>() {
            @Override
            public void onSuccess(OnvifDevice device, String subscriptionRef) {
                subscriptionReference = subscriptionRef;
                System.out.println("Mozgásérzékelési feliratkozás létrehozva: " + subscriptionRef);
                
                // Most már lekérdezhetjük az eseményeket
                pullMotionEvents();
            }

            @Override
            public void onError(OnvifRequest.OnvifException exception) {
                System.err.println("Hiba a feliratkozás létrehozása során: " + exception.getMessage());
            }
        });
    }

    /**
     * Példa a mozgásérzékelési események lekérdezésére
     */
    public void pullMotionEvents() {
        if (subscriptionReference == null) {
            System.err.println("Nincs aktív feliratkozás!");
            return;
        }

        onvifManager.pullMessages(device, subscriptionReference, 20, 60, new OnvifRequest.Listener<List<OnvifMotionEvent>>() {
            @Override
            public void onSuccess(OnvifDevice device, List<OnvifMotionEvent> events) {
                System.out.println("Mozgásérzékelési események fogadva: " + events.size() + " esemény");
                
                for (OnvifMotionEvent event : events) {
                    System.out.println("Esemény típusa: " + event.getEventType());
                    System.out.println("Időpont: " + event.getTimestamp());
                    System.out.println("Forrás: " + event.getSourceToken());
                    System.out.println("Bizalmi szint: " + event.getConfidence());
                    
                    if (event.getDetectionArea() != null) {
                        OnvifMotionDetectionArea area = event.getDetectionArea();
                        System.out.println("Érzékelt terület: x=" + area.getX() + ", y=" + area.getY() + 
                                         ", szélesség=" + area.getWidth() + ", magasság=" + area.getHeight());
                    }
                    System.out.println("---");
                }
            }

            @Override
            public void onError(OnvifRequest.OnvifException exception) {
                System.err.println("Hiba az események lekérdezése során: " + exception.getMessage());
            }
        });
    }

    // OnvifMotionDetectionListener implementáció
    @Override
    public void onMotionDetectionConfigurationReceived(OnvifDevice device, OnvifMotionDetection motionDetection) {
        System.out.println("Mozgásérzékelési konfiguráció fogadva: " + motionDetection.getName());
    }

    @Override
    public void onMotionDetectionConfigurationSet(OnvifDevice device) {
        System.out.println("Mozgásérzékelési konfiguráció beállítva");
    }

    @Override
    public void onAnalyticsEnginesReceived(OnvifDevice device, List<String> analyticsEngines) {
        System.out.println("Analytics engine-ek fogadva: " + analyticsEngines.size() + " engine");
    }

    @Override
    public void onMotionDetectionSubscriptionCreated(OnvifDevice device, String subscriptionReference) {
        System.out.println("Mozgásérzékelési feliratkozás létrehozva: " + subscriptionReference);
    }

    @Override
    public void onMotionEventsReceived(OnvifDevice device, List<OnvifMotionEvent> events) {
        System.out.println("Mozgásérzékelési események fogadva: " + events.size() + " esemény");
    }

    @Override
    public void onMotionDetectionError(OnvifDevice device, String error) {
        System.err.println("Mozgásérzékelési hiba: " + error);
    }

    // OnvifMotionEventListener implementáció
    @Override
    public void onMotionEvent(OnvifDevice device, OnvifMotionEvent event) {
        System.out.println("Mozgásérzékelési esemény: " + event.getEventType() + " - " + event.getTimestamp());
    }

    @Override
    public void onMotionEventError(OnvifDevice device, String error) {
        System.err.println("Mozgásérzékelési esemény hiba: " + error);
    }

    /**
     * Példa használat
     */
    public static void main(String[] args) {
        MotionDetectionExample example = new MotionDetectionExample();
        
        // Eszköz beállítása (példa)
        example.device = new OnvifDevice("192.168.1.100", "admin", "password");
        
        // Mozgásérzékelési funkciók tesztelése
        example.getAnalyticsEngines();
        example.getMotionDetectionConfiguration();
        example.setMotionDetectionConfiguration();
        example.createMotionDetectionSubscription();
    }
}

