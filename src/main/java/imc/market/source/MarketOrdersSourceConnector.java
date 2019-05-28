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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.source.SourceConnector;

/**
 * Connector Source that consume market orders from a public service, performs data enrichment and sends it over
 * down the pipeline.
 *
 * The orders are streamed from:
 * https://www.pubnub.com/developers/realtime-data-streams/financial-securities-market-orders/
 */
public class MarketOrdersSourceConnector extends SourceConnector {

    /** PubNub stream name. */
    protected final static String STREAM_NAME_PROP = "stream.name";

    /** PubNub stream subscription key. */
    protected final static String STREAM_SUBSCRIPION_KEY = "stream.subscription.key";

    /** Kafka topic name. */
    protected final static String TOPIC_NAME_PROP = "topic.name";

    /** */
    private String streamName = "pubnub-market-orders";

    /** */
    private String topicName = "market_orders";

    /** */
    private String subscriptionKey = "sub-c-4377ab04-f100-11e3-bffd-02ee2ddab7fe";


    @Override
    public void start(Map<String, String> props) {
        streamName = props.get(STREAM_NAME_PROP);
        topicName = props.get(TOPIC_NAME_PROP);
        subscriptionKey = props.get(STREAM_SUBSCRIPION_KEY);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return MarketOrdersSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();

        // Only one input partition makes sense.
        Map<String, String> config = new HashMap<>();

        config.put(STREAM_NAME_PROP, streamName);
        config.put(TOPIC_NAME_PROP, topicName);
        config.put(STREAM_SUBSCRIPION_KEY, subscriptionKey);

        configs.add(config);

        return configs;
    }

    @Override
    public void stop() {
        //No actions. The Task will be terminated separately.
    }

    @Override
    public ConfigDef config() {
        ConfigDef config = new ConfigDef();

        config.define(STREAM_NAME_PROP, ConfigDef.Type.STRING, ConfigDef.Importance.MEDIUM,
            "PubNup market orders stream name");

        config.define(STREAM_SUBSCRIPION_KEY, ConfigDef.Type.STRING, ConfigDef.Importance.MEDIUM,
            "PubNup market orders stream's subscription key");

        config.define(TOPIC_NAME_PROP, ConfigDef.Type.STRING, ConfigDef.Importance.LOW,
            "Kafka topic name for the market orders");

        return config;
    }

    public String version() {
        return "1.0";
    }

    /**
     * MarketOrders custom schema.
     */
    protected static class MarketOrderSchema {
        /**
         * Returns market order schema.
         * @return
         */
        protected static Schema schema() {

            return SchemaBuilder.struct().name("MarketOrder")
                .field("symbol", Schema.STRING_SCHEMA)
                .field("order_quantity", Schema.INT32_SCHEMA)
                .field("bid_price", Schema.FLOAT64_SCHEMA)
                .field("trade_type", Schema.STRING_SCHEMA)
                .field("timestamp", org.apache.kafka.connect.data.Timestamp.SCHEMA)
                .build();
        }
    }
}
