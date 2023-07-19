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
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.xlibb.mqtt.utils.ModuleUtils.getModule;
import static io.xlibb.mqtt.utils.MqttConstants.CALLER;
import static io.xlibb.mqtt.utils.MqttConstants.DUPLICATE;
import static io.xlibb.mqtt.utils.MqttConstants.MESSAGE_ID;
import static io.xlibb.mqtt.utils.MqttConstants.ONERROR;
import static io.xlibb.mqtt.utils.MqttConstants.ONMESSAGE;
import static io.xlibb.mqtt.utils.MqttConstants.PAYLOAD;
import static io.xlibb.mqtt.utils.MqttConstants.QOS;
import static io.xlibb.mqtt.utils.MqttConstants.RECORD_MESSAGE;
import static io.xlibb.mqtt.utils.MqttConstants.RETAINED;
import static io.xlibb.mqtt.utils.MqttConstants.SUBSCRIBER;

/**
 * Class containing the callback of Mqtt subscriber.
 */
public class MqttCallbackImpl implements MqttCallback {

    private final Runtime runtime;
    private final BObject service;
    private final MqttClient subscriber;

    public MqttCallbackImpl(Runtime runtime, BObject service, MqttClient subscriber) {
        this.runtime = runtime;
        this.service = service;
        this.subscriber = subscriber;
    }

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        BError mqttError = MqttUtils.createMqttError(disconnectResponse.getException());
        invokeOnError(mqttError);
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        BError mqttError = MqttUtils.createMqttError(exception);
        invokeOnError(mqttError);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        invokeOnMessage(message);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {}

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {}

    @Override
    public void deliveryComplete(IMqttToken token) {}

    private void invokeOnMessage(MqttMessage message) {
        BMap<BString, Object> bMqttMessage = getBMqttMessage(message);
        StrandMetadata metadata = getStrandMetadata(ONMESSAGE);
        CountDownLatch latch = new CountDownLatch(1);
        boolean callerExists = isCallerAvailable();
        boolean onMessageImplemented = isOnMessageImplemented();
        if (!onMessageImplemented) {
            invokeOnError(MqttUtils.createMqttError(new NoSuchMethodException("method onMessage not found")));
            return;
        }
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

    private void invokeOnError(BError bError) {
        boolean onErrorImplemented = isOnErrorImplemented();
        if (!onErrorImplemented) {
            bError.printStackTrace();
            return;
        }
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

    private boolean isOnErrorImplemented() {
        Optional<RemoteMethodType> onErrorMethodType = getRemoteMethodType(ONERROR);
        return onErrorMethodType.isPresent();
    }

    private boolean isOnMessageImplemented() {
        Optional<RemoteMethodType> onMessageMethodType = getRemoteMethodType(ONMESSAGE);
        return onMessageMethodType.isPresent();
    }

    private boolean isCallerAvailable() {
        Optional<RemoteMethodType> onMessageMethodType = getRemoteMethodType(ONMESSAGE);
        return onMessageMethodType.isPresent() && onMessageMethodType.get().getType().getParameters().length == 2;
    }

    private Optional<RemoteMethodType> getRemoteMethodType(String methodName) {
        RemoteMethodType[] methodTypes = ((ServiceType) service.getOriginalType()).getRemoteMethods();
        for (RemoteMethodType methodType: methodTypes) {
            if (methodType.getName().equals(methodName)) {
                return Optional.of(methodType);
            }
        }
        return Optional.empty();
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

    private StrandMetadata getStrandMetadata(String parentFunctionName) {
        Module module = ModuleUtils.getModule();
        return new StrandMetadata(module.getOrg(), module.getName(), module.getMajorVersion(), parentFunctionName);
    }
}
