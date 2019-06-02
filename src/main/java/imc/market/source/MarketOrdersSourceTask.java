/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package imc.market.source;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

/**
 * Connector Source task that receives and processes market orders data.
 */
public class MarketOrdersSourceTask extends SourceTask {

    /** Stream object. */
    private PubNub stream;

    /** Stream name. */
    private String streamName;

    /** Kafka topic name. */
    private String topicName;

    /** Offset generator. */
    private AtomicLong counter = new AtomicLong();

    /** Up to 1000 last records. */
    private ArrayList<SourceRecord> lastRecords = new ArrayList<>();

    /** Synchronization monitor. */
    private final Object monitor = new Object();

    @Override
    public void start(Map<String, String> props) {
        topicName = props.get(MarketOrdersSourceConnector.TOPIC_NAME_PROP);
        streamName = props.get(MarketOrdersSourceConnector.STREAM_NAME_PROP);

        PNConfiguration cfg = new PNConfiguration();
        cfg.setSubscribeKey(props.get(MarketOrdersSourceConnector.STREAM_SUBSCRIPION_KEY));


        stream = new PubNub(cfg);

        stream.addListener(new StreamCallback());
        stream.subscribe().channels(Arrays.asList(streamName)).execute();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        ArrayList<SourceRecord> records;

        synchronized (monitor) {
            records = lastRecords;

            lastRecords = new ArrayList<>();
        }

        return records;
    }

    @Override
    public void stop() {
        stream.unsubscribe().execute();
    }

    @Override
    public String version() {
        return "1.0";
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
            JsonObject json = mes.getAsJsonObject();

            synchronized (monitor) {
                if (lastRecords.size() >= 1000) {
                    // Back pressure logic
                    System.out.println("Skipping:" + json);

                    return;
                }

                Schema keySchema = MarketOrdersSourceConnector.MarketOrderSchema.keySchema();
                Struct key = new Struct(keySchema);

                key.put("id", counter.incrementAndGet());
                key.put("buyer_id", new Random().nextInt(6) + 1);

                Schema valSchema = MarketOrdersSourceConnector.MarketOrderSchema.valueSchema();
                Struct val = new Struct(valSchema);

                val.put("symbol", json.get("symbol").getAsString());
                val.put("order_quantity", json.get("order_quantity").getAsInt());
                val.put("bid_price", json.get("bid_price").getAsDouble());
                val.put("trade_type", json.get("trade_type").getAsString());
                val.put("timestamp", new Timestamp(json.get("timestamp").getAsLong() * 1000));

                Map sourcePartition = Collections.singletonMap("stream", streamName);
                Map sourceOffset = Collections.singletonMap("position", key.get("id"));

                SourceRecord record = new SourceRecord(sourcePartition, sourceOffset, topicName,
                    keySchema, key, valSchema, val);

                lastRecords.add(record);
            }
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
