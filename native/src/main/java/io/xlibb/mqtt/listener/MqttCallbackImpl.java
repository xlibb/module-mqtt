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
import static io.xlibb.mqtt.utils.MqttConstants.CALLER;
import static io.xlibb.mqtt.utils.MqttConstants.DUPLICATE;
import static io.xlibb.mqtt.utils.MqttConstants.GRANTED_QOS;
import static io.xlibb.mqtt.utils.MqttConstants.MESSAGE;
import static io.xlibb.mqtt.utils.MqttConstants.MESSAGE_ID;
import static io.xlibb.mqtt.utils.MqttConstants.ONCOMPLETE;
import static io.xlibb.mqtt.utils.MqttConstants.ONERROR;
import static io.xlibb.mqtt.utils.MqttConstants.ONMESSAGE;
import static io.xlibb.mqtt.utils.MqttConstants.PAYLOAD;
import static io.xlibb.mqtt.utils.MqttConstants.QOS;
import static io.xlibb.mqtt.utils.MqttConstants.RECORD_DELIVERY_TOKEN;
import static io.xlibb.mqtt.utils.MqttConstants.RECORD_MESSAGE;
import static io.xlibb.mqtt.utils.MqttConstants.RETAINED;
import static io.xlibb.mqtt.utils.MqttConstants.SUBSCRIBER;
import static io.xlibb.mqtt.utils.MqttConstants.TOPICS;

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
        StrandMetadata metadata = getStrandMetadata(ONCOMPLETE);
        CountDownLatch latch = new CountDownLatch(1);
        runtime.invokeMethodAsyncSequentially(service, ONCOMPLETE, null, metadata,
                new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY, bMqttMessage, true);
        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private void invokeOnMessage(MqttMessage message) {
        BMap<BString, Object> bMqttMessage = getBMqttMessage(message);
        StrandMetadata metadata = getStrandMetadata(ONMESSAGE);
        CountDownLatch latch = new CountDownLatch(1);
        boolean callerExists = isCallerAvailable();
        if (callerExists) {
            BObject callerObject = ValueCreator.createObjectValue(getModule(), CALLER);
            callerObject.addNativeData(SUBSCRIBER, subscriber);
            callerObject.addNativeData(MESSAGE_ID, message.getId());
            callerObject.addNativeData(QOS, message.getQos());
            runtime.invokeMethodAsyncSequentially(service, ONMESSAGE, null, metadata,
                    new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY,
                    bMqttMessage, true, callerObject, true);
        } else {
            runtime.invokeMethodAsyncSequentially(service, ONMESSAGE, null, metadata,
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
            if (methodType.getName().equals(ONMESSAGE)) {
                return Optional.of(methodType);
            }
        }
        return Optional.empty();
    }

    private void invokeOnError(BError bError) {
        StrandMetadata metadata = getStrandMetadata(ONERROR);
        CountDownLatch latch = new CountDownLatch(1);
        runtime.invokeMethodAsyncSequentially(service, ONERROR, null, metadata,
                new BServiceInvokeCallbackImpl(latch), null, PredefinedTypes.TYPE_ANY, bError, true);
        try {
            latch.await(100, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private BMap<BString, Object> getBMqttMessage(MqttMessage message) {
        BMap<BString, Object> bMessage = ValueCreator.createRecordValue(getModule(), RECORD_MESSAGE);
        bMessage.put(StringUtils.fromString(PAYLOAD), ValueCreator.createArrayValue(message.getPayload()));
        bMessage.put(StringUtils.fromString(MESSAGE_ID), message.getId());
        bMessage.put(StringUtils.fromString(QOS), message.getQos());
        bMessage.put(StringUtils.fromString(RETAINED), message.isRetained());
        bMessage.put(StringUtils.fromString(DUPLICATE), message.isDuplicate());
        return bMessage;
    }

    private BMap<BString, Object> getMqttDeliveryToken(IMqttDeliveryToken token) throws MqttException {
        MqttMessage mqttMessage = token.getMessage();
        BMap<BString, Object> bMessage = getBMqttMessage(mqttMessage);
        BMap<BString, Object> bDeliveryToken = ValueCreator.createRecordValue(getModule(), RECORD_DELIVERY_TOKEN);
        bDeliveryToken.put(MESSAGE, bMessage);
        long[] qosArray = Arrays.stream(token.getGrantedQos()).asLongStream().toArray();
        bDeliveryToken.put(GRANTED_QOS, ValueCreator.createArrayValue(qosArray));
        bDeliveryToken.put(StringUtils.fromString(MESSAGE_ID), token.getMessageId());
        bDeliveryToken.put(TOPICS, StringUtils.fromStringArray(token.getTopics()));
        return bDeliveryToken;
    }

    private StrandMetadata getStrandMetadata(String parentFunctionName) {
        Module module = ModuleUtils.getModule();
        return new StrandMetadata(module.getOrg(), module.getName(), module.getMajorVersion(), parentFunctionName);
    }
}
