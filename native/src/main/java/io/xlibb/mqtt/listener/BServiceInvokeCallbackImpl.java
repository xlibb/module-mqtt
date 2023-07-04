package io.xlibb.mqtt.listener;

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.CountDownLatch;

public class BServiceInvokeCallbackImpl implements Callback {

    private final CountDownLatch countDownLatch;

    public BServiceInvokeCallbackImpl(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void notifySuccess(Object obj) {
        if (obj instanceof BError) {
            ((BError) obj).printStackTrace();
        }
        countDownLatch.countDown();
    }

    @Override
    public void notifyFailure(BError bError) {
        bError.printStackTrace();
        countDownLatch.countDown();
        System.exit(1);
    }
}
