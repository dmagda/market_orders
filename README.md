# Market Orders Streaming and Analytics

Confluent Kafka and GridGain/Ignite Market Orders Streaming demo.

## GridGain Cluster Setup and Launch

### Launching the Demo Cluster
* Download and decompose [GridGain Enterprise Edition 8.7.5 or later](https://www.gridgain.com/resources/download)
* Move `{gridgain}/libs/optional/ignite-rest-http` folder to `{gridgain}/bin/libs` 
* Navigate to `{gridgain}/bin` folder and start a server node(s): `./ignite.sh {demo_project_root}/market_orders/cfg/gridgain-cfg.xml`
* Start `Bootstrapper` class from your idea to create demo tables and preload initial data. Plus, the cluster will
be activated if it was started for the first time. The activation is required for clusters with Ignite Persistence.

### Monitoring the Demo Cluster
* Sign up on `console.gridgain.com` and go to Monitoring screen. Download web-agent and decompose it.
* Navigate to `web-agent` folder and execute `./ignite-web-agent.sh`
* Go back to `console.gridgain.com` and refresh Monitoring screen. 
* Now you have the cluster started and can monitor it!


## Confluent Platform and GridGain Setup

If Java 9+ is used by default then switch to Java 8 in a command line window to be used for Confluent
scripts. Use this command on Mac OS:
export JAVA_HOME="`/usr/libexec/java_home -v '1.8.0_191'`"

### Install Confluent and GridGain Connect
* Download and install Confluent Platform 5.2.1 or later following 
[this guide](https://docs.confluent.io/current/connect/quickstart.html#connect-quickstart)
* Edit `{confluent}/etc/schema-registry/schema-registry.properties` file by updating `listeners` property to
 `listeners=http://0.0.0.0:9081`. This is to avoid port conflicts between Confluent Schema-Registry and 
 GridGain nodes.
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
* Load GridGain Sink Connector:
`./confluent load gridgain-sink -d {demo_project_root}/market_orders/cfg/gridgain-sink.properties`
* Check that the Connector is up and running: `./confluent status gridgain-sink`
* Load MarketOrders Source Connector:
`./confluent load market-orders -d {demo_project_root}/market_orders/cfg/market-orders-source.properties`
* Check that the Source Connector is running: `./confluent status market-orders`

### Logging and Debugging

Use the command below to see the latest logs: `./confluent log connect -f`