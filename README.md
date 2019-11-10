# Market Orders Streaming and Analytics

Confluent Kafka and GridGain/Ignite Market Orders Streaming demo.

## GridGain Cluster Setup and Launch

### Launching the Demo Cluster
* Download and decompose [GridGain Enterprise Edition 8.7.5 or later](https://www.gridgain.com/resources/download)
* Move `{gridgain}/libs/optional/ignite-rest-http` folder to `{gridgain}/bin/libs` 
* Navigate to `{gridgain}/bin` folder and start a server node(s): `./ignite.sh -J-DIGNITE_JETTY_PORT=9080 {demo_project_root}/market_orders/cfg/gridgain-cfg.xml`
* Start `Bootstrapper` class from your IDE to create demo tables and preload initial data. Plus, the cluster will
be activated if it was started for the first time. The activation is required for clusters with Ignite Persistence.

### Monitoring the Demo Cluster
* Sign up on `console.gridgain.com` and go to Monitoring screen. Download web-agent and decompose it.
* Navigate to `web-agent`, open `default.properties` and update `node-uri` parameter to `node-uri=http://localhost:9080`
* Execute `./ignite-web-agent.sh`
* Go back to `console.gridgain.com` and refresh Monitoring screen. 
* Now you have the cluster started and can monitor it!


## Confluent Platform and GridGain Setup

If Java 9+ is used by default then switch to Java 8 in a command line window to be used for Confluent
scripts. Use this command on Mac OS:
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/"

### Install Confluent and GridGain Connect
* Go to `{gridgain}/integration/gridgain-kafka-connect/` and execute `./copy-dependencies.sh ` script from there
* Download and install Confluent Platform 5.2.1 or later following 
[this guide](https://docs.confluent.io/current/connect/quickstart.html#connect-quickstart)
* Go to `{confluent}/share/java` and create folder `gridgain-sink` there
* Copy GridGain Connect into `gridgain-sink` with the following command:
`cp -R {gridgain}/integration/gridgain-kafka-connect/lib/ .`

### Prepare MarketOrder Source Connector
* Build this project's JAR 
[with IntellijIdea](https://stackoverflow.com/questions/36303535/intellij-build-build-artifacts-deactivated)
or from your favourite IDE.
* Go back to Go to `{confluent}/share/java` folder and create `market-orders` folder there
* Copy MarketOrders Source connector into `market-orders` with the following command:
`cp -R {demo_project_root}/market_orders/out/artifacts/market_jar/market.jar .`

### Starting Confluent, Loading Source and Sink
* Go to `{confluent}/bin` folder and start Confluent CLI with default clusters: `./confluent start`
* Update `igniteCfg` parameter with an actual path to the `grdgain-cfg.xml` in the file below
`{demo_project_root}/market_orders/cfg/gridgain-sink.properties`
* Load GridGain Sink Connector:
`./confluent load gridgain-sink -d {demo_project_root}/market_orders/cfg/gridgain-sink.properties`
* Check that the Connector is up and running: `./confluent status gridgain-sink`
* Load MarketOrders Source Connector:
`./confluent load market-orders -d {demo_project_root}/market_orders/cfg/market-orders-source.properties`
* Check that the Source Connector is running: `./confluent status market-orders`

Go to Confluent Web Console (`http://localhost:9021`) and demonstrate the following queries:

`select symbol, sum(order_quantity) as total, bid_price
from MARKETORDER  WINDOW TUMBLING (SIZE 10 seconds) 
where bid_price>500.0
group by symbol, bid_price
having sum(order_quantity) > 10;`


`SELECT symbol, SUM(bid_price) as mp
FROM MarketOrder GROUP BY(symbol);`

### Real-Time Analytics with GridGain

Events will be generated and arriving in the cluster in real-time. Run sample queries like those below for
the demo purpose:

Total spendings per buyer:

  `SELECT first_name, last_name, SUM(bid_price) as mp
  FROM MarketOrder as m JOIN Buyer as b ON m.buyer_id = b.id
  GROUP BY (first_name, last_name) ORDER BY mp DESC;` 
  
Cash Distribution (fraction of money spent per symbol):
  
  `SELECT symbol, SUM(bid_price) as mp
   FROM MarketOrder GROUP BY(symbol) ORDER BY mp DESC; `
   

Restart GridGain cluster (activate with WebConsole or the CMD tool if needed) and show that the day
is still in the cluster.

### Logging and Debugging

Use the command below to see the latest logs: `./confluent log connect -f`