package imc.market.streams;

import com.google.gson.JsonElement;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import imc.market.gridgain.IMCStorage;
import java.util.Arrays;

/**
 * Class that is subscribed to a real-time stream of market order:
 * https://www.pubnub.com/developers/realtime-data-streams/financial-securities-market-orders/
 */
public class MarketOrdersStream {

    private PNConfiguration cfg;

    private String channelName = "pubnub-market-orders";

    private PubNub stream;

    private IMCStorage storage;

    /*

     */
    public MarketOrdersStream(IMCStorage storage) {
        // A public Market Orders real-time stream is used for the sake of the demo:
        // https://www.pubnub.com/developers/realtime-data-streams/financial-securities-market-orders/

        cfg = new PNConfiguration();
        cfg.setSubscribeKey("sub-c-4377ab04-f100-11e3-bffd-02ee2ddab7fe");

        this.storage = storage;
    }

    /*

     */
    public synchronized void stream() {
        if (stream == null) {
            stream = new PubNub(cfg);

            stream.addListener(new StreamCallback());

            stream.subscribe().channels(Arrays.asList(channelName)).execute();
        }
    }

    /*

     */
    public synchronized void stop() {
        if (stream != null) {
            stream.unsubscribe().execute();
            stream = null;
        }
    }

    private class StreamCallback extends SubscribeCallback {

        /**
         *
         * @param nub
         * @param status
         */
        public void status(PubNub nub, PNStatus status) {

            if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                // Connect event.
                System.out.println("Connected to the market orders stream: " + status.toString());
            }
            else if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                System.err.println("Connection is lost:" + status.getErrorData().toString());
            }
            else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                // Happens as part of our regular operation. This event happens when
                // radio / connectivity is lost, then regained.
                System.out.println("Reconnected to the market orders stream");
            }
            else {
                System.out.println("Connection status changes:" + status.toString());
            }
        }

        /**
         *
         * @param nub
         * @param result
         */
        public void message(PubNub nub, PNMessageResult result) {
            JsonElement mes = result.getMessage();

            System.out.println(mes.toString());

            storage.storeMarketOrder(mes.getAsJsonObject());
        }

        /**
         *
         * @param nub
         * @param result
         */
        public void presence(PubNub nub, PNPresenceEventResult result) {
            System.out.println("Stream presence event: " + result.toString());
        }
    }
}
