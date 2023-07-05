import ballerina/jballerina.java;

public client isolated class Client {

    public isolated function init(string serverUri, string clientId, *ClientConfiguration config) returns Error? {
        check self.externInit(serverUri, clientId, config);
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
