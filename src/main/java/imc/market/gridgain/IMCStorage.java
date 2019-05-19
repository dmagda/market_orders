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

    private AtomicLong counter;

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
            "CREATE TABLE MarketOrder (" +
                "id long PRIMARY KEY," +
                "symbol varchar," +
                "order_quantity int," +
                "bid_price double," +
                "trade_type int," +
                "order_date timestamp) WITH \"backups=1\"").setSchema("PUBLIC")).getAll();

        counter = new AtomicLong();
    }

    public void storeMarketOrder(JsonObject order) {
        gatewayCache.query(new SqlFieldsQuery(
            "INSERT INTO MarketOrder (id, symbol, order_quantity, bid_price, trade_type, order_date)" +
            " VALUES (" + counter.getAndIncrement() + ", " +
                "'" + order.get("symbol").getAsString() + "', " +
                order.get("order_quantity").getAsInt() + ", " +
                order.get("bid_price").getAsDouble() + ", " +
                tradeTypeStrToInt(order.get("trade_type").getAsString()) + ", " +
                "'" + new Timestamp(order.get("timestamp").getAsLong() * 1000) + "'" +
                ")").setSchema("PUBLIC")).getAll();
    }

    private int tradeTypeStrToInt(String tradeType) {
        if (tradeType.equalsIgnoreCase("day"))
            return 1;

        if (tradeType.equalsIgnoreCase("stop"))
            return 2;

        if (tradeType.equalsIgnoreCase("fill or kill"))
            return 3;

        if (tradeType.equalsIgnoreCase("market"))
            return 4;

        if (tradeType.equalsIgnoreCase("limit"))
            return 5;

        throw new RuntimeException("Unknown trade type:" + tradeType);
    }
}
