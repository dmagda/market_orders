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

package imc.market.gridgain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

public class IMCStorage {

    private Ignite ignite;

    private IgniteCache gatewayCache;

    /**
     *
     */
    public IMCStorage() {
    }

    public void init() {
        Ignition.setClientMode(true);

        ignite = Ignition.start("cfg/gridgain-cfg.xml");

        ignite.active(true);

        CacheConfiguration cfg = new CacheConfiguration("tempCache");
        cfg.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES));

        gatewayCache = ignite.getOrCreateCache(cfg);

        gatewayCache.query(new SqlFieldsQuery(
            "DROP TABLE IF EXISTS MarketOrder").setSchema("PUBLIC")).getAll();

        gatewayCache.query(new SqlFieldsQuery(
            "DROP TABLE IF EXISTS Buyer").setSchema("PUBLIC")).getAll();

        gatewayCache.query(new SqlFieldsQuery(
            "CREATE TABLE MarketOrder (" +
                "id long," +
                "buyer_id int," +
                "symbol varchar," +
                "order_quantity int," +
                "bid_price double," +
                "trade_type varchar," +
                "order_date timestamp," +
                "PRIMARY KEY(id, buyer_id)) " +
                "WITH \"backups=1, cache_name=MarketOrder, affinity_key=buyer_id, " +
                "key_type=MarketOrderKey, value_type=MarketOrder\"").
            setSchema("PUBLIC")).getAll();

        gatewayCache.query(new SqlFieldsQuery(
            "CREATE TABLE Buyer (" +
                "id int PRIMARY KEY," +
                "first_name varchar," +
                "last_name varchar," +
                "age int," +
                "goverment_id varchar) " +
                "WITH \"backups=1, cache_name=Buyer, value_type=Buyer\""
        ).setSchema("PUBLIC")).getAll();

        SqlFieldsQuery query = new SqlFieldsQuery(
            "INSERT INTO Buyer (id, first_name, last_name, age, goverment_id) VALUES (?,?,?,?,?)").
            setSchema("PUBLIC");

        gatewayCache.query(query.setArgs(1, "John", "Smith", 45, "7bfjd73"));
        gatewayCache.query(query.setArgs(2, "Arnold", "Mazer", 55, "unb23212"));
        gatewayCache.query(query.setArgs(3, "Lara", "Croft", 35, "12338fb31"));
        gatewayCache.query(query.setArgs(4, "Patrick", "Green", 42, "asbn233"));
        gatewayCache.query(query.setArgs(5, "Anna", "Romanoff", 46, "klnnk3823"));
        gatewayCache.query(query.setArgs(6, "Alfred", "Black", 55, "32345"));

        ignite.close();
    }
}
