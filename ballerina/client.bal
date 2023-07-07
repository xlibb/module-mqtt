import ballerina/jballerina.java;

# Represents the client that is used to publish messages to the server.
public client isolated class Client {

    # Creates a new `mqtt:Client`.
    #
    # + serverUri - URI of the server to connect to
    # + clientId - Unique ID of the client
    # + config - Optional configuration values to use for the client
    # + return - `mqtt:Error` if an error occurs while creating the client
    public isolated function init(string serverUri, string clientId, *ClientConfiguration config) returns Error? {
        check self.externInit(serverUri, clientId, config);
    }

    # Publishes a message to a topic.
    #
    # + topic - Topic to publish the message to
    # + message - Message to publish
    # + return - `mqtt:Error` if an error occurs while publishing
    isolated remote function publish(string topic, Message message) returns Error? {
        check self.externPublish(topic, message);
    }

    # Closes the connection to the server.
    # + return - `mqtt:Error` if an error occurs while closing
    isolated remote function close() returns Error? {
        check self.externClose();
    }

    # Checks if the client is connected to the server.
    # + return - `true` if the client is connected, `mqtt:Error` if an error occurs in the process
    isolated remote function isConnected() returns boolean|Error {
        return self.externIsConnected();
    }
    
    # Disconnects the client from the server.
    # + return - `mqtt:Error` if an error occurs while disconnecting
    isolated remote function disconnect() returns Error? {
        check self.externDisconnect();
    }

    # Reconnects the client to the server.
    # + return - `mqtt:Error` if an error occurs while reconnecting
    isolated remote function reconnect() returns Error? {
        check self.externReconnect();
    }

    private isolated function externInit(string serverUri, string clientId, *ClientConfiguration config) returns Error? =
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
