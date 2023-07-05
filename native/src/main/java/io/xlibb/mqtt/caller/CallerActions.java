package io.xlibb.mqtt.caller;

import io.ballerina.runtime.api.values.BObject;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import static io.xlibb.mqtt.utils.MqttUtils.createMqttError;

/**
 * Class containing the external methods of the caller.
 */
public class CallerActions {

    public static Object complete(BObject callerObject) {
        IMqttClient subscriber = (IMqttClient) callerObject.getNativeData("subscriber");
        int messageId = (int) callerObject.getNativeData("messageId");
        int qos = (int) callerObject.getNativeData("qos");
        try {
            subscriber.messageArrivedComplete(messageId, qos);
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

}
