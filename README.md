# Ballerina MQTT Library

[![Build](https://github.com/xlibb/module-mqtt/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/xlibb/module-mqtt/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/xlibb/module-mqtt/branch/master/graph/badge.svg)](https://codecov.io/gh/xlibb/module-mqtt)
[![Trivy](https://github.com/xlibb/module-mqtt/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/xlibb/module-mqtt/actions/workflows/trivy-scan.yml)
[![GraalVM Check](https://github.com/xlibb/module-mqtt/actions/workflows/build-with-bal-test-graalvm.yml/badge.svg)](https://github.com/xlibb/module-mqtt/actions/workflows/build-with-bal-test-graalvm.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/xlibb/module-mqtt.svg)](https://github.com/xlibb/module-mqtt/commits/master)

This Library provides an implementation to interact with MQTT servers via MQTT client and listener.

MQTT is a lightweight, publish-subscribe, machine to machine network protocol for message queue/message queuing service.

### Publisher and subscriber
#### MQTT publisher
A MQTT publisher is a MQTT client that publishes messages to the MQTT server. When working with a MQTT client, the first thing to do is to initialize the client.
For the publisher to work successfully, an active MQTT server should be available.

The code snippet given below initializes a publisher client with the basic configuration.
```ballerina
import xlibb/mqtt;
import ballerina/uuid;
 
mqtt:ClientConfiguration clientConfiguration = {
    connectionConfig: {
        username: "ballerina",
        password: "ballerinamqtt"
    }
};

mqtt:Client mqttClient = check new (mqtt:DEFAULT_URL, uuid:createType1AsString(), clientConfiguration); // A unique id needs to be provided as the client id
```
Using the `publish` api of this client, messages can be sent to the MQTT server.
```ballerina
check mqttClient->publish("mqtt/test", {payload: "This is Ballerina MQTT client!!!".toBytes()});
```
#### MQTT subscriber
A MQTT subscriber is a client responsible for reading messages from one or more topics in the server. When working with a MQTT subscriber, the first thing to do is initialize the subscriber.
For the subscriber to work successfully, an active MQTT server should be available.

The code snippet given below initializes a subscriber with the basic configuration.
```ballerina
mqtt:ListenerConfiguration listenerConfiguration = {
    connectionConfig: {
        username: "ballerina",
        password: "ballerinamqtt"
    },
    manualAcks: false   // When set to false, the MQTT acknowledgements are not sent automatically by the subscriber
};

mqtt:Listener mqttSubscriber = check new (mqtt:DEFAULT_URL, uuid:createType1AsString(), "mqtt/test", listenerConfiguration);
```
This subscriber can be used in the `mqtt:Service` to listen to messages in `mqtt/test` topic.
```ballerina
service on mqttSubscriber {
    remote function onMessage(mqtt:Message message, mqtt:Caller caller) returns error? {
        log:printInfo(check string:fromBytes(message.payload));
        check caller->complete();
    }

    remote function onError(mqtt:Error err) returns error? {
        log:printError("Error occured ", err);
    }

    remote function onCompleted(mqtt:DeliveryToken token) returns error? {
        log:printInfo(string`Message ${token.messageId.toString()} delivered`);
        log:printInfo(check string:fromBytes(token.message.payload));
    }
}
```
The `mqtt:Caller` can be used to indicate that the application has completed processing the message by using `complete()` api.
