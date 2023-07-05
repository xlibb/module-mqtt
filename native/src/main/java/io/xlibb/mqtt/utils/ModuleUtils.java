package io.xlibb.mqtt.utils;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;

/**
 * Class holding module related utility functions.
 */
public class ModuleUtils {

    private static Module mqttModule = null;

    private ModuleUtils() {
    }

    public static void setModule(Environment env) {
        mqttModule = env.getCurrentModule();
    }

    public static Module getModule() {
        return mqttModule;
    }
}
