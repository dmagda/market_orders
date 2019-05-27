# Market Orders Streaming and Analytics

Confluent Kafka and GridGain/Ignite Market Orders Streaming demo.


# Installation

Follow this section to set up an environment for the demo.

## GridGain
* Download and decompose GridGain Enterprise Edition 8.7.5 or later: https://www.gridgain.com/resources/download
* Move `{gridgain}/libs/optional/ignite-rest-http` folder to `{gridgain}/bin/libs` 
* Navigate to `{gridgain}/bin` folder and start a server node(s): `./ignite.sh {root_directory_of_the_demo}/market_orders/cfg/gridgain-cfg.xml`
* Sign up on `console.gridgain.com` and go to Monitoring screen. Download web-agent and decompose it.
* Navigate to `web-agent` folder and execute `./ignite-web-agent.sh`
* Go back to `console.gridgain.com` and refresh Monitoring screen. 
* Now you have the cluster started and can monitor it!

For more details refer to https://docs.gridgain.com/docs

GridGain Kafka Connect (the code is to be integrated in the demo): https://docs.gridgain.com/docs/certified-kafka-connector-quickstart

## Confluent Connector
TODO

* Switch Java version for Confluent to 8:
`export JAVA_HOME="`/usr/libexec/java_home -v '1.8.0_191'`"`

## Demo
* Create a project with IntellijIdea and launch `Bootstrapper`