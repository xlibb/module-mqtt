package io.xlibb.mqtt.utils;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Contains the constant values related in the runtime.
 */
public class MqttConstants {

    public static final BString CONNECTION_CONFIGURATION = StringUtils.fromString("connectionConfig");
    public static final BString USERNAME = StringUtils.fromString("username");
    public static final BString PASSWORD = StringUtils.fromString("password");
    public static final BString MAX_RECONNECT_DELAY = StringUtils.fromString("maxReconnectDelay");
    public static final BString KEEP_ALIVE_INTERVAL = StringUtils.fromString("keepAliveInterval");
    public static final BString MAX_INFLIGHT = StringUtils.fromString("maxInflight");
    public static final BString CONNECTION_TIMEOUT = StringUtils.fromString("connectionTimeout");
    public static final BString CLEAN_SESSION = StringUtils.fromString("cleanSession");
    public static final BString SERVER_URIS = StringUtils.fromString("serverUris");
    public static final BString AUTOMATIC_RECONNECT = StringUtils.fromString("automaticReconnect");
    public static final BString SECURE_SOCKET = StringUtils.fromString("secureSocket");
    public static final BString CERT = StringUtils.fromString("cert");
    public static final BString MESSAGE = StringUtils.fromString("message");
    public static final BString GRANTED_QOS = StringUtils.fromString("grantedQos");
    public static final BString TOPICS = StringUtils.fromString("topics");

    public static final String ERROR_NAME = "Error";

    public static final String CLIENT_OBJECT = "clientObject";
    public static final String SUBSCRIBER = "subscriber";
    public static final String MESSAGE_ID = "messageId";
    public static final String QOS = "qos";
    public static final String PAYLOAD = "payload";
    public static final String RETAINED = "retained";
    public static final String DUPLICATE = "duplicate";
    public static final String MANUAL_ACKS = "manualAcks";
    public static final String SERVICE = "service";
    public static final String CALLER = "Caller";
    public static final String RECORD_MESSAGE = "Message";
    public static final String RECORD_DELIVERY_TOKEN = "DeliveryToken";
    
    public static final String ONCOMPLETE = "onComplete";
    public static final String ONMESSAGE = "onMessage";
    public static final String ONERROR = "onError";

}
