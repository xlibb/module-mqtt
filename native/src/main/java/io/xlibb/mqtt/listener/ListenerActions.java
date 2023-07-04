package io.xlibb.mqtt.listener;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class ListenerActions {

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

    public static Object externAttach(Environment environment, BObject clientObject, BObject service, Object topics) {
        clientObject.addNativeData("service", service);
        IMqttClient subscriber = (IMqttClient) clientObject.getNativeData("clientObject");
        subscriber.setCallback(new MqttCallbackImpl(environment.getRuntime(), service));
        return null;
    }

    public static Object externDetach(BObject clientObject, BObject service) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            publisher.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        clientObject.addNativeData("service", null);
        return null;
    }

    public static Object externStart(BObject clientObject, BArray topics) {
        IMqttClient subscriber = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            subscriber.subscribe(topics.getStringArray());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object externGracefulStop(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            publisher.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        clientObject.addNativeData("service", null);
        return null;
    }

    public static Object externImmediateStop(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            publisher.disconnectForcibly();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        clientObject.addNativeData("service", null);
        return null;
    }

}
