import ballerina/jballerina.java;

public client isolated class Caller {

    isolated remote function complete() returns Error? =
    @java:Method {
        'class: "io.xlibb.mqtt.caller.CallerActions"
    } external;
}
