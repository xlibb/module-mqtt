import ballerina/jballerina.java;

# Represents a Kafka producer endpoint.
#
# + connectorId - Unique ID for a particular connector to use in trasactions
# + producerConfig - Stores configurations related to a Kafka connection
public client isolated class Client {

    # Creates a new `kafka:Producer`.
    #
    # + bootstrapServers - List of remote server endpoints of Kafka brokers
    # + config - Configurations related to initializing a `kafka:Producer`
    # + return - A `kafka:Error` if closing the producer failed or else '()'
    public isolated function init() returns Error? {
        check self.externInit();
    }

    isolated remote function publish(string topic, Message message) returns Error? {
        check self.externPublish(topic, message);
    }

    isolated remote function close() returns Error? {
        check self.externClose();
    }

    isolated remote function isConnected() returns boolean|Error {
        return self.externIsConnected();
    }
    
    isolated remote function disconnect() returns Error? {
        check self.externDisconnect();
    }

    isolated remote function reconnect() returns Error? {
        check self.externReconnect();
    }

    private isolated function externInit() returns Error? =
    @java:Method {
        'class: "io.xlibb.mqtt.client.ClientActions"
    } external;

    private isolated function externPublish(string topic, Message message) returns Error? =
    @java:Method {
        'class: "io.xlibb.mqtt.client.ClientActions"
    } external;

    private isolated function externClose() returns Error? =
    @java:Method {
        'class: "io.xlibb.mqtt.client.ClientActions"
    } external;

    private isolated function externIsConnected() returns boolean|Error =
    @java:Method {
        'class: "io.xlibb.mqtt.client.ClientActions"
    } external;

    private isolated function externDisconnect() returns Error? =
    @java:Method {
        'class: "io.xlibb.mqtt.client.ClientActions"
    } external;

    private isolated function externReconnect() returns Error? =
    @java:Method {
        'class: "io.xlibb.mqtt.client.ClientActions"
    } external;
}
