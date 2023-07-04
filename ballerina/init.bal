import ballerina/jballerina.java;

function init() {
    setModule();
    //initializeLoggingConfigurations();
}

function setModule() = @java:Method {
    'class: "io.xlibb.mqtt.utils.ModuleUtils"
} external;

//function initializeLoggingConfigurations() = @java:Method {
//    'class: "io.ballerina.stdlib.kafka.utils.ModuleUtils"
//} external;

