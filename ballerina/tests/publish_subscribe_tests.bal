import ballerina/log;
import ballerina/lang.runtime;
import ballerina/test;
import ballerina/uuid;

final string[] receivedMessages = [];

final Service basicService = service object {
    remote function onMessage(Message message) returns error? {
        log:printInfo(check string:fromBytes(message.payload));
        receivedMessages.push(check string:fromBytes(message.payload));
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
    Listener 'listener = check new (NO_AUTH_ENDPOINT, uuid:createType1AsString(), "mqtt/basictest");
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (NO_AUTH_ENDPOINT, uuid:createType1AsString());
    string message = "Test message for basic pub sub test";
    check 'client->publish("mqtt/basictest", {payload: message.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    lock {
    test:assertTrue(receivedMessages.indexOf(message) > -1);
    }
}

@test:Config {enable: true}
function basicPublishSubscribeWithAuthTest() returns error? {
    Listener 'listener = check new (AUTH_ONLY_ENDPOINT, uuid:createType1AsString(), "mqtt/basictest", {connectionConfig: authConnConfig});
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (AUTH_ONLY_ENDPOINT, uuid:createType1AsString(), {connectionConfig: authConnConfig});
    string message = "Test message for basic pub sub with auth test";
    check 'client->publish("mqtt/basictest", {payload: message.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    test:assertTrue(receivedMessages.indexOf(message) > -1);
}

@test:Config {enable: true}
function basicPublishSubscribeWithTLSTest() returns error? {
    Listener 'listener = check new (NO_AUTH_ENCRYPTED_ENDPOINT, uuid:createType1AsString(), "mqtt/basictest", {connectionConfig: tlsConnConfig});
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (NO_AUTH_ENCRYPTED_ENDPOINT, uuid:createType1AsString(), {connectionConfig: tlsConnConfig});
    string message = "Test message for basic pub sub with tls test";
    check 'client->publish("mqtt/basictest", {payload: message.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    test:assertTrue(receivedMessages.indexOf(message) > -1);
}

@test:Config {enable: true}
function basicPublishSubscribeWithMTLSTest() returns error? {
    Listener 'listener = check new (NO_AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), "mqtt/basictest", {connectionConfig: mtlsConnConfig});
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (NO_AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), {connectionConfig: mtlsConnConfig});
    string message = "Test message for basic pub sub with mtls test";
    check 'client->publish("mqtt/basictest", {payload: message.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    test:assertTrue(receivedMessages.indexOf(message) > -1);
}

@test:Config {enable: true}
function basicPublishSubscribeWithAuthAndMTLSTest() returns error? {
    Listener 'listener = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), "mqtt/basictest", {connectionConfig: authMtlsConnConfig});
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), {connectionConfig: authMtlsConnConfig});
    string message = "Test message for basic pub sub with auth and mtls test";
    check 'client->publish("mqtt/basictest", {payload: message.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    test:assertTrue(receivedMessages.indexOf(message) > -1);
}

@test:Config {enable: true}
function subscribeToMultipleTopicsTest() returns error? {
    Listener 'listener = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), ["mqtt/topic1", "mqtt/topic2"], {connectionConfig: authMtlsConnConfig});
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), {connectionConfig: authMtlsConnConfig});
    string message1 = "Test message for topic 1";
    string message2 = "Test message for topic 1";
    check 'client->publish("mqtt/topic1", {payload: message1.toBytes()});
    check 'client->publish("mqtt/topic2", {payload: message1.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    test:assertTrue(receivedMessages.indexOf(message1) > -1);
    test:assertTrue(receivedMessages.indexOf(message2) > -1);
}

@test:Config {enable: true}
function subscribeToSubscriptionTest() returns error? {
    Listener 'listener = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), {topic: "mqtt/subscriptiontopic", qos: 2}, {connectionConfig: authMtlsConnConfig});
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), {connectionConfig: authMtlsConnConfig});
    string message = "Test message for subscription";
    check 'client->publish("mqtt/subscriptiontopic", {payload: message.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    test:assertTrue(receivedMessages.indexOf(message) > -1);
}

@test:Config {enable: true}
function subscribeToMultipleSubscriptionsTest() returns error? {
    Listener 'listener = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), [{topic: "mqtt/subscriptiontopic1", qos: 2}, {topic: "mqtt/subscriptiontopic2", qos: 0}], {connectionConfig: authMtlsConnConfig});
    check 'listener.attach(basicService);
    check 'listener.'start();

    Client 'client = check new (AUTH_MTLS_ENDPOINT, uuid:createType1AsString(), {connectionConfig: authMtlsConnConfig});
    string message1 = "Test message for subscription1";
    string message2 = "Test message for subscription2";
    check 'client->publish("mqtt/subscriptiontopic1", {payload: message1.toBytes()});
    check 'client->publish("mqtt/subscriptiontopic2", {payload: message2.toBytes()});
    runtime:sleep(1);

    check stopListenerAndClient('listener, 'client);

    test:assertTrue(receivedMessages.indexOf(message1) > -1);
    test:assertTrue(receivedMessages.indexOf(message2) > -1);
}

function stopListenerAndClient(Listener 'listener, Client 'client) returns error? {
    check 'client->disconnect();
    check 'client->close();
    check 'listener.gracefulStop();
}
