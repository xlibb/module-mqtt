import ballerina/log;
import ballerina/lang.runtime;
import ballerina/test;
import ballerina/uuid;

string receivedMessage = "";

service on new Listener(DEFAULT_URL, uuid:createType1AsString(), "mqtt/test", {
    connectionConfig: {
        username: "ballerina",
        password: "ballerinamqtt",
        connectionTimeout: 100
    }
}) {
    remote function onMessage(Message message) returns error? {
        log:printInfo(check string:fromBytes(message.payload));
        receivedMessage = check string:fromBytes(message.payload);
    }

    remote function onError(Error err) returns error? {
        log:printError("Error occured ", err);
    }

    remote function onCompleted(DeliveryToken token) returns error? {
        log:printInfo("Message delivered " + token.messageId.toString());
        log:printInfo(check string:fromBytes(token.message.payload));
    }
}

@test:Config {enable: true}
function basicPublishSubscribeTest() returns error? {
    Client 'client = check new (DEFAULT_URL, uuid:createType1AsString(), {
        connectionConfig: {
            username: "ballerina",
            password: "ballerinamqtt",
            connectionTimeout: 100
        }
    });
    check 'client->publish("mqtt/test", {payload: "This is a test message".toBytes()});
    runtime:sleep(1);
    test:assertEquals(receivedMessage, "This is a test message");
}
