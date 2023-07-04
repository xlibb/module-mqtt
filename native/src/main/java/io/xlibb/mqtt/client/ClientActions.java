package io.xlibb.mqtt.client;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class ClientActions {

    public static Object externInit(BObject clientObject) {
        try {
            IMqttClient publisher = new MqttClient("tcp://localhost:1883",
                    UUID.randomUUID().toString(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            publisher.connect(options);
            clientObject.addNativeData("clientObject", publisher);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object externPublish(BObject clientObject, BString topic, BMap message) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        byte[] payload = ((BArray)message.get(StringUtils.fromString("payload"))).getByteArray();
        try {
            publisher.publish(topic.getValue(), new MqttMessage(payload));
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object externClose(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            publisher.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object externIsConnected(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        return publisher.isConnected();
    }

    public static Object externDisconnect(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            publisher.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object externReconnect(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            publisher.reconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }
}
