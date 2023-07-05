package io.xlibb.mqtt.listener;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static io.xlibb.mqtt.utils.MqttUtils.createMqttError;
import static io.xlibb.mqtt.utils.MqttUtils.getMqttConnectOptions;

/**
 * Class containing the external methods of the listener.
 */
public class ListenerActions {

    public static Object externInit(BObject clientObject, BString serverUri, BString clientId,
                                    BMap<BString, Object> clientConfiguration) {
        try {

            IMqttClient subscriber = new MqttClient(serverUri.getValue(), clientId.getValue(), new MemoryPersistence());
            MqttConnectOptions options = getMqttConnectOptions(clientConfiguration);
            subscriber.connect(options);
            clientObject.addNativeData("clientObject", subscriber);
        } catch (MqttException e) {
            return createMqttError(e);
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
            return createMqttError(e);
        }
        clientObject.addNativeData("service", null);
        return null;
    }

    public static Object externStart(BObject clientObject, BArray topics) {
        IMqttClient subscriber = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            subscriber.subscribe(topics.getStringArray());
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externGracefulStop(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            publisher.disconnect();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        clientObject.addNativeData("service", null);
        return null;
    }

    public static Object externImmediateStop(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData("clientObject");
        try {
            publisher.disconnectForcibly();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        clientObject.addNativeData("service", null);
        return null;
    }

}
