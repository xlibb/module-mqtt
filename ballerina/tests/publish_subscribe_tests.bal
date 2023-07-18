import ballerina/log;
import ballerina/lang.runtime;
import ballerina/test;
import ballerina/uuid;

string receivedMessage = "";

Service basicService = service object {
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
};

@test:Config {enable: true}
function basicPublishSubscribeTest() returns error? {
    Listener 'listener = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), "mqtt/basictest", {connectionConfig: authMtlsConnConfig});
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), {connectionConfig: authMtlsConnConfig});
    string message = "This is a test message for basic pub sub test";
    check 'client->publish("mqtt/basictest", {payload: message.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    test:assertEquals(receivedMessage, message);
}

function stopListenerAndClient(Listener 'listener, Client 'client) returns error? {
    check 'client->disconnect();
    check 'client->close();
    check 'listener.gracefulStop();
}
