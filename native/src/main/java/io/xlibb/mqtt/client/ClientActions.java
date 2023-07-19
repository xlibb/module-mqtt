package io.xlibb.mqtt.client;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

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
            MqttClient publisher = new MqttClient(serverUri.getValue(), clientId.getValue(), new MemoryPersistence());
            MqttConnectionOptions options = getMqttConnectOptions(clientConfiguration);
            publisher.connect(options);
            clientObject.addNativeData(CLIENT_OBJECT, publisher);
        } catch (BError e) {
            return e;
        } catch (Exception e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externPublish(BObject clientObject, BString topic, BMap message) {
        MqttClient publisher = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        MqttMessage mqttMessage = generateMqttMessage(message);
        try {
            publisher.publish(topic.getValue(), mqttMessage);
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externClose(BObject clientObject) {
        MqttClient publisher = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        try {
            publisher.close();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externIsConnected(BObject clientObject) {
        MqttClient publisher = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        return publisher.isConnected();
    }

    public static Object externDisconnect(BObject clientObject) {
        MqttClient publisher = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        try {
            publisher.disconnect();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externReconnect(BObject clientObject) {
        MqttClient publisher = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
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
