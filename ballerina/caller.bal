import ballerina/jballerina.java;

# Represents the client that is used to complete received messages.
public client isolated class Caller {

    # Completes the received message.
    # 
    # + return - `mqtt:Error` if the message cannot be completed
    isolated remote function complete() returns Error? =
    @java:Method {
        'class: "io.xlibb.mqtt.caller.CallerActions"
    } external;
}
