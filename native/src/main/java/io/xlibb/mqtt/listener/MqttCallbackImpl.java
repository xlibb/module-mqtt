package io.xlibb.mqtt.listener;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.xlibb.mqtt.utils.ModuleUtils;
import io.xlibb.mqtt.utils.MqttUtils;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.xlibb.mqtt.utils.ModuleUtils.getModule;

/**
 * Class containing the callback of Mqtt subscriber.
 */
public class MqttCallbackImpl implements MqttCallback {

    private final Runtime runtime;
    private final BObject service;
    private final IMqttClient subscriber;

    public MqttCallbackImpl(Runtime runtime, BObject service, IMqttClient subscriber) {
        this.runtime = runtime;
        this.service = service;
        this.subscriber = subscriber;
    }

    @Override
    public void connectionLost(Throwable cause) {
        BError mqttError = MqttUtils.createMqttError(cause);
        invokeOnError(mqttError);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        invokeOnMessage(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        BMap<BString, Object> bMqttMessage;
        try {
            bMqttMessage = getMqttDeliveryToken(token);
        } catch (MqttException e) {
            BError bError = MqttUtils.createMqttError(e);
            invokeOnError(bError);
            return;
        }
        StrandMetadata metadata = getStrandMetadata("onComplete");
        CountDownLatch latch = new CountDownLatch(1);
        runtime.invokeMethodAsyncSequentially(service, "onComplete", null, metadata,
                new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY, bMqttMessage, true);
        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private void invokeOnMessage(MqttMessage message) {
        BMap<BString, Object> bMqttMessage = getBMqttMessage(message);
        StrandMetadata metadata = getStrandMetadata("onMessage");
        CountDownLatch latch = new CountDownLatch(1);
        boolean callerExists = isCallerAvailable();
        if (callerExists) {
            BObject callerObject = ValueCreator.createObjectValue(getModule(), "Caller");
            callerObject.addNativeData("subscriber", subscriber);
            callerObject.addNativeData("messageId", message.getId());
            callerObject.addNativeData("qos", message.getQos());
            runtime.invokeMethodAsyncSequentially(service, "onMessage", null, metadata,
                    new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY,
                    bMqttMessage, true, callerObject, true);
        } else {
            runtime.invokeMethodAsyncSequentially(service, "onMessage", null, metadata,
                    new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY, bMqttMessage, true);
        }
        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private boolean isCallerAvailable() {
        Optional<RemoteMethodType> onMessageMethodType = getOnMessageMethodType();
        return onMessageMethodType.isPresent() && onMessageMethodType.get().getType().getParameters().length == 2;
    }

    private Optional<RemoteMethodType> getOnMessageMethodType() {
        RemoteMethodType[] methodTypes = ((ServiceType) service.getOriginalType()).getRemoteMethods();
        for (RemoteMethodType methodType: methodTypes) {
            if (methodType.getName().equals("onMessage")) {
                return Optional.of(methodType);
            }
        }
        return Optional.empty();
    }

    private void invokeOnError(BError bError) {
        StrandMetadata metadata = getStrandMetadata("onError");
        CountDownLatch latch = new CountDownLatch(1);
        runtime.invokeMethodAsyncSequentially(service, "onError", null, metadata,
                new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY, bError, true);
        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private BMap<BString, Object> getBMqttMessage(MqttMessage message) {
        BMap<BString, Object> bMessage = ValueCreator.createRecordValue(getModule(), "Message");
        bMessage.put(StringUtils.fromString("payload"), ValueCreator.createArrayValue(message.getPayload()));
        bMessage.put(StringUtils.fromString("messageId"), message.getId());
        bMessage.put(StringUtils.fromString("qos"), message.getQos());
        bMessage.put(StringUtils.fromString("retained"), message.isRetained());
        bMessage.put(StringUtils.fromString("duplicate"), message.isDuplicate());
        return bMessage;
    }

    private BMap<BString, Object> getMqttDeliveryToken(IMqttDeliveryToken token) throws MqttException {
        MqttMessage mqttMessage = token.getMessage();
        BMap<BString, Object> bMessage = getBMqttMessage(mqttMessage);
        BMap<BString, Object> bDeliveryToken = ValueCreator.createRecordValue(getModule(), "DeliveryToken");
        bDeliveryToken.put(StringUtils.fromString("message"), bMessage);
        long[] qosArray = Arrays.stream(token.getGrantedQos()).asLongStream().toArray();
        bDeliveryToken.put(StringUtils.fromString("grantedQos"), ValueCreator.createArrayValue(qosArray));
        bDeliveryToken.put(StringUtils.fromString("messageId"), token.getMessageId());
        bDeliveryToken.put(StringUtils.fromString("topics"), StringUtils.fromStringArray(token.getTopics()));
        return bDeliveryToken;
    }

    private StrandMetadata getStrandMetadata(String parentFunctionName) {
        Module module = ModuleUtils.getModule();
        return new StrandMetadata(module.getOrg(), module.getName(), module.getMajorVersion(), parentFunctionName);
    }
}
