package io.xlibb.mqtt.caller;

import io.ballerina.runtime.api.values.BObject;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import static io.xlibb.mqtt.utils.MqttConstants.MESSAGE_ID;
import static io.xlibb.mqtt.utils.MqttConstants.QOS;
import static io.xlibb.mqtt.utils.MqttConstants.SUBSCRIBER;
import static io.xlibb.mqtt.utils.MqttUtils.createMqttError;

/**
 * Class containing the external methods of the caller.
 */
public class CallerActions {

    public static Object complete(BObject callerObject) {
        IMqttClient subscriber = (IMqttClient) callerObject.getNativeData(SUBSCRIBER);
        int messageId = (int) callerObject.getNativeData(MESSAGE_ID);
        int qos = (int) callerObject.getNativeData(QOS);
        try {
            subscriber.messageArrivedComplete(messageId, qos);
        } catch (MqttException e) {
            return createMqttError(e);
        }
        return null;
    }

}
