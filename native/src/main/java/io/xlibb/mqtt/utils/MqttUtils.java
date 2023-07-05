package io.xlibb.mqtt.utils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import static io.xlibb.mqtt.utils.ModuleUtils.getModule;

/**
 * Class containing the utility functions related to the clients.
 */
public class MqttUtils {

    public static MqttConnectOptions getMqttConnectOptions(BMap<BString, Object> configuration) {
        MqttConnectOptions options = new MqttConnectOptions();
        Object connectionConfigObject = configuration.get(StringUtils.fromString("connectionConfig"));
        if (connectionConfigObject != null && connectionConfigObject instanceof BMap) {
            BMap<BString, Object> connectionConfig = (BMap<BString, Object>) connectionConfigObject;
            Object username = connectionConfig.get(StringUtils.fromString("username"));
            if (username != null) {
                options.setUserName(((BString) username).getValue());
            }
            Object password = connectionConfig.get(StringUtils.fromString("password"));
            if (password != null) {
                options.setPassword(((BString) password).getValue().toCharArray());
            }
            Object maxReconnectDelay = connectionConfig.get(StringUtils.fromString("maxReconnectDelay"));
            if (maxReconnectDelay != null) {
                options.setMaxReconnectDelay(((Long) maxReconnectDelay).intValue());
            }
            Object keepAliveInterval = connectionConfig.get(StringUtils.fromString("keepAliveInterval"));
            if (keepAliveInterval != null) {
                options.setKeepAliveInterval(((Long) keepAliveInterval).intValue());
            }
            Object maxInflight = connectionConfig.get(StringUtils.fromString("maxInflight"));
            if (maxInflight != null) {
                options.setMaxInflight(((Long) maxInflight).intValue());
            }
            Object connectionTimeout = connectionConfig.get(StringUtils.fromString("connectionTimeout"));
            if (connectionTimeout != null) {
                options.setConnectionTimeout(((Long) connectionTimeout).intValue());
            }
            Object cleanSession = connectionConfig.get(StringUtils.fromString("cleanSession"));
            if (cleanSession != null) {
                options.setCleanSession((boolean) cleanSession);
            }
            Object serverUris = connectionConfig.get(StringUtils.fromString("serverUris"));
            if (serverUris != null) {
                options.setServerURIs(((BArray) serverUris).getStringArray());
            }
            Object automaticReconnect = connectionConfig.get(StringUtils.fromString("automaticReconnect"));
            if (automaticReconnect != null) {
                options.setAutomaticReconnect((boolean) automaticReconnect);
            }
        }
        return options;
    }

    public static BError createMqttError(Exception exception) {
        BError cause = ErrorCreator.createError(exception.getCause());
        return ErrorCreator.createError(getModule(), "Error",
                StringUtils.fromString(exception.getMessage()), cause, null);
    }

    public static BError createMqttError(Throwable throwable) {
        BError cause = ErrorCreator.createError(throwable);
        return ErrorCreator.createError(getModule(), "Error",
                StringUtils.fromString(throwable.getMessage()), cause, null);
    }
}
