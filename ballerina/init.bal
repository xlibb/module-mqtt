import ballerina/jballerina.java;

function init() {
    setModule();
}

function setModule() = @java:Method {
    'class: "io.xlibb.mqtt.utils.ModuleUtils"
} external;
