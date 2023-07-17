package io.xlibb.mqtt.listener;

import io.ballerina.runtime.api.Environment;
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
import org.eclipse.paho.mqttv5.common.MqttSubscription;

import static io.xlibb.mqtt.utils.MqttConstants.BQOS;
import static io.xlibb.mqtt.utils.MqttConstants.CLIENT_OBJECT;
import static io.xlibb.mqtt.utils.MqttConstants.MANUAL_ACKS;
import static io.xlibb.mqtt.utils.MqttConstants.SERVICE;
import static io.xlibb.mqtt.utils.MqttConstants.TOPIC;
import static io.xlibb.mqtt.utils.MqttUtils.createMqttError;
import static io.xlibb.mqtt.utils.MqttUtils.getMqttConnectOptions;

/**
 * Class containing the external methods of the listener.
 */
public class ListenerActions {

    public static Object externInit(BObject clientObject, BString serverUri, BString clientId,
                                    BMap<BString, Object> listenerConfiguration) {
        try {
            MqttClient subscriber = new MqttClient(serverUri.getValue(), clientId.getValue(), new MemoryPersistence());
            MqttConnectionOptions options = getMqttConnectOptions(listenerConfiguration);
            boolean manualAcks = listenerConfiguration.getBooleanValue(StringUtils.fromString(MANUAL_ACKS));
            subscriber.setManualAcks(manualAcks);
            subscriber.connect(options);
            clientObject.addNativeData(CLIENT_OBJECT, subscriber);
        } catch (MqttException e) {
            return createMqttError(e);
        } catch (BError e) {
            return e;
        }
        return null;
    }

    public static Object externAttach(Environment environment, BObject clientObject, BObject service, Object topics) {
        clientObject.addNativeData("service", service);
        MqttClient subscriber = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        subscriber.setCallback(new MqttCallbackImpl(environment.getRuntime(), service, subscriber));
        return null;
    }

    public static Object externDetach(BObject clientObject, BObject service) {
        MqttClient subscriber = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        try {
            subscriber.disconnect();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        clientObject.addNativeData(SERVICE, null);
        return null;
    }

    public static Object externStart(BObject clientObject, BArray subscriptions) {
        MqttClient subscriber = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        MqttSubscription[] mqttSubscriptions = new MqttSubscription[subscriptions.size()];
        for (int i = 0; i < subscriptions.size(); i++) {
            BMap topicSubscription = (BMap) subscriptions.getValues()[i];
            mqttSubscriptions[i] = new MqttSubscription(topicSubscription.getStringValue(TOPIC).getValue(),
                    topicSubscription.getIntValue(BQOS).intValue());
        }
        try {
            subscriber.subscribe(mqttSubscriptions);
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

    public static Object externGracefulStop(BObject clientObject) {
        MqttClient subscriber = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        try {
            subscriber.disconnect();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        clientObject.addNativeData(SERVICE, null);
        return null;
    }

    public static Object externImmediateStop(BObject clientObject) {
        MqttClient subscriber = (MqttClient) clientObject.getNativeData(CLIENT_OBJECT);
        try {
            subscriber.disconnectForcibly();
        } catch (MqttException e) {
            return createMqttError(e);
        }
        clientObject.addNativeData(SERVICE, null);
        return null;
    }

}
