package io.xlibb.mqtt.listener;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.xlibb.mqtt.utils.ModuleUtils;
import io.xlibb.mqtt.utils.MqttUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.xlibb.mqtt.utils.ModuleUtils.getModule;

/**
 * Class containing the callback of Mqtt subscriber.
 */
public class MqttCallbackImpl implements MqttCallback {

    private final Runtime runtime;
    private final BObject service;

    public MqttCallbackImpl(Runtime runtime, BObject service) {
        this.runtime = runtime;
        this.service = service;
    }

    @Override
    public void connectionLost(Throwable cause) {
        BError mqttError = MqttUtils.createMqttError(cause);
        StrandMetadata metadata = getStrandMetadata("onError");
        CountDownLatch latch = new CountDownLatch(1);
        runtime.invokeMethodAsyncSequentially(service, "onError", null, metadata,
                new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY, mqttError, true);
        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        BMap<BString, Object> bMqttMessage = getBMqttMessage(message);
        StrandMetadata metadata = getStrandMetadata("onMessage");
        CountDownLatch latch = new CountDownLatch(1);
        runtime.invokeMethodAsyncSequentially(service, "onMessage", null, metadata,
                new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY, bMqttMessage, true);
        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private BMap<BString, Object> getBMqttMessage(MqttMessage message) {
        BMap<BString, Object> bMessage = ValueCreator.createRecordValue(getModule(), "Message");
        byte[] payload = message.getPayload();
        int messageId = message.getId();
        int qos = message.getQos();
        boolean retained = message.isRetained();
        boolean duplicate = message.isDuplicate();
        bMessage.put(StringUtils.fromString("payload"), ValueCreator.createArrayValue(payload));
        bMessage.put(StringUtils.fromString("messageId"), messageId);
        bMessage.put(StringUtils.fromString("qos"), qos);
        bMessage.put(StringUtils.fromString("retained"), retained);
        bMessage.put(StringUtils.fromString("duplicate"), duplicate);
        return bMessage;
    }

    private StrandMetadata getStrandMetadata(String parentFunctionName) {
        Module module = ModuleUtils.getModule();
        return new StrandMetadata(module.getOrg(), module.getName(), module.getMajorVersion(), parentFunctionName);
    }
}
