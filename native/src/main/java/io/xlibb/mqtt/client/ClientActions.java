package io.xlibb.mqtt.client;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static io.xlibb.mqtt.utils.MqttConstants.CLIENT_OBJECT;
import static io.xlibb.mqtt.utils.MqttConstants.PAYLOAD;
import static io.xlibb.mqtt.utils.MqttConstants.QOS;
import static io.xlibb.mqtt.utils.MqttConstants.RETAINED;
import static io.xlibb.mqtt.utils.MqttUtils.createMqttError;
import static io.xlibb.mqtt.utils.MqttUtils.getMqttConnectOptions;

/**
 * Class containing the external methods of the publisher.
 */
public class ClientActions {

    public static Object externInit(BObject clientObject, BString serverUri, BString clientId,
                                    BMap<BString, Object> clientConfiguration) {
        try {
            IMqttClient publisher = new MqttClient(serverUri.getValue(), clientId.getValue(), new MemoryPersistence());
            MqttConnectOptions options = getMqttConnectOptions(clientConfiguration);
            publisher.connect(options);
            clientObject.addNativeData(CLIENT_OBJECT, publisher);
        } catch (MqttException e) {
            return createMqttError(e);
        } catch (BError e) {
            return e;
        }
        return null;
    }

    public static Object externPublish(BObject clientObject, BString topic, BMap message) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        MqttMessage mqttMessage = generateMqttMessage(message);
        try {
            publisher.publish(topic.getValue(), mqttMessage);
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externClose(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        try {
            publisher.close();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externIsConnected(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        return publisher.isConnected();
    }

    public static Object externDisconnect(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        try {
            publisher.disconnect();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externReconnect(BObject clientObject) {
        IMqttClient publisher = (IMqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        try {
            publisher.reconnect();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    private static MqttMessage generateMqttMessage(BMap message) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(((BArray) message.get(StringUtils.fromString(PAYLOAD))).getByteArray());
        mqttMessage.setQos(((Long) message.get(StringUtils.fromString(QOS))).intValue());
        mqttMessage.setRetained(((boolean) message.get(StringUtils.fromString(RETAINED))));
        return mqttMessage;
    }
}
