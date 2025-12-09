package be.teletask.onvif.requests;

import be.teletask.onvif.models.OnvifType;

/**
 * ONVIF Unsubscribe kérés egy PullPoint / SubscriptionManager feliratkozás
 * megszüntetésére.
 */
public class UnsubscribeRequest implements OnvifRequest<Void> {

    //Constants
    public static final String TAG = UnsubscribeRequest.class.getSimpleName();

    //Attributes
    private final Listener<Void> listener;
    private final String subscriptionReference;

    //Constructors
    public UnsubscribeRequest(String subscriptionReference, Listener<Void> listener) {
        super();
        this.subscriptionReference = subscriptionReference;
        this.listener = listener;
    }

    //Properties
    @Override
    public Listener<Void> getListener() {
        return listener;
    }

    /**
     * A SubscriptionManager / PullPoint teljes URL-je, amelyet a
     * CreatePullPointSubscription válaszban az Address mezőben kaptunk.
     */
    public String getSubscriptionReference() {
        return subscriptionReference;
    }

    @Override
    public String getSoapHeader() {
        return "<wsa:To xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" + subscriptionReference + "</wsa:To>";
    }

    @Override
    public String getXml() {
        return "<wsnt:Unsubscribe xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.UNSUBSCRIBE;
    }
}



