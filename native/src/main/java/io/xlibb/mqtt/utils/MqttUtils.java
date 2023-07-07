package io.xlibb.mqtt.utils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import static io.xlibb.mqtt.utils.ModuleUtils.getModule;
import static io.xlibb.mqtt.utils.MqttConstants.AUTOMATIC_RECONNECT;
import static io.xlibb.mqtt.utils.MqttConstants.CERT;
import static io.xlibb.mqtt.utils.MqttConstants.CLEAN_SESSION;
import static io.xlibb.mqtt.utils.MqttConstants.CONNECTION_CONFIGURATION;
import static io.xlibb.mqtt.utils.MqttConstants.CONNECTION_TIMEOUT;
import static io.xlibb.mqtt.utils.MqttConstants.ERROR_NAME;
import static io.xlibb.mqtt.utils.MqttConstants.KEEP_ALIVE_INTERVAL;
import static io.xlibb.mqtt.utils.MqttConstants.MAX_INFLIGHT;
import static io.xlibb.mqtt.utils.MqttConstants.MAX_RECONNECT_DELAY;
import static io.xlibb.mqtt.utils.MqttConstants.PASSWORD;
import static io.xlibb.mqtt.utils.MqttConstants.SECURE_SOCKET;
import static io.xlibb.mqtt.utils.MqttConstants.SERVER_URIS;
import static io.xlibb.mqtt.utils.MqttConstants.USERNAME;

/**
 * Class containing the utility functions related to the clients.
 */
public class MqttUtils {

    public static MqttConnectOptions getMqttConnectOptions(BMap<BString, Object> configuration) {
        MqttConnectOptions options = new MqttConnectOptions();
        Object connectionConfigObject = configuration.get(CONNECTION_CONFIGURATION);
        if (connectionConfigObject != null && connectionConfigObject instanceof BMap) {
            BMap<BString, Object> connectionConfig = (BMap<BString, Object>) connectionConfigObject;
            Object username = connectionConfig.get(USERNAME);
            if (username != null) {
                options.setUserName(((BString) username).getValue());
            }
            Object password = connectionConfig.get(PASSWORD);
            if (password != null) {
                options.setPassword(((BString) password).getValue().toCharArray());
            }
            Object maxReconnectDelay = connectionConfig.get(MAX_RECONNECT_DELAY);
            if (maxReconnectDelay != null) {
                options.setMaxReconnectDelay(((Long) maxReconnectDelay).intValue());
            }
            Object keepAliveInterval = connectionConfig.get(KEEP_ALIVE_INTERVAL);
            if (keepAliveInterval != null) {
                options.setKeepAliveInterval(((Long) keepAliveInterval).intValue());
            }
            Object maxInflight = connectionConfig.get(MAX_INFLIGHT);
            if (maxInflight != null) {
                options.setMaxInflight(((Long) maxInflight).intValue());
            }
            Object connectionTimeout = connectionConfig.get(CONNECTION_TIMEOUT);
            if (connectionTimeout != null) {
                options.setConnectionTimeout(((Long) connectionTimeout).intValue());
            }
            Object cleanSession = connectionConfig.get(CLEAN_SESSION);
            if (cleanSession != null) {
                options.setCleanSession((boolean) cleanSession);
            }
            Object serverUris = connectionConfig.get(SERVER_URIS);
            if (serverUris != null) {
                options.setServerURIs(((BArray) serverUris).getStringArray());
            }
            Object automaticReconnect = connectionConfig.get(AUTOMATIC_RECONNECT);
            if (automaticReconnect != null) {
                options.setAutomaticReconnect((boolean) automaticReconnect);
            }
            Object secureSocket = connectionConfig.get(SECURE_SOCKET);
            if (secureSocket != null) {
                SocketFactory socketFactory = getSocketFactory((BMap<BString, Object>) secureSocket);
                options.setSocketFactory(socketFactory);
            }
        }
        return options;
    }

    private static SocketFactory getSocketFactory(BMap<BString, Object> secureSocket) {
        String certPath = secureSocket.getStringValue(CERT).getValue();
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setCertificateEntry("Custom CA", CertificateFactory.getInstance("X509")
                    .generateCertificate(new FileInputStream(certPath)));

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, null);
            return sslContext.getSocketFactory();
        } catch (IOException | CertificateException | KeyStoreException |
                NoSuchAlgorithmException | KeyManagementException e) {
            throw createMqttError(e);
        }
    }

    public static BError createMqttError(Exception exception) {
        BError cause = ErrorCreator.createError(exception.getCause());
        return ErrorCreator.createError(getModule(), ERROR_NAME,
                StringUtils.fromString(exception.getMessage()), cause, null);
    }

    public static BError createMqttError(Throwable throwable) {
        BError cause = ErrorCreator.createError(throwable);
        return ErrorCreator.createError(getModule(), ERROR_NAME,
                StringUtils.fromString(throwable.getMessage()), cause, null);
    }
}
