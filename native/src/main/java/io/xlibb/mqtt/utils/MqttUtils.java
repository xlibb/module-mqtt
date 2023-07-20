package io.xlibb.mqtt.utils;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.crypto.nativeimpl.Decode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.UUID;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static io.xlibb.mqtt.utils.ModuleUtils.getModule;
import static io.xlibb.mqtt.utils.MqttConstants.AUTOMATIC_RECONNECT;
import static io.xlibb.mqtt.utils.MqttConstants.CERT;
import static io.xlibb.mqtt.utils.MqttConstants.CERT_FILE;
import static io.xlibb.mqtt.utils.MqttConstants.CLEAN_START;
import static io.xlibb.mqtt.utils.MqttConstants.CONNECTION_CONFIGURATION;
import static io.xlibb.mqtt.utils.MqttConstants.CONNECTION_TIMEOUT;
import static io.xlibb.mqtt.utils.MqttConstants.CRYPTO_TRUSTSTORE_PASSWORD;
import static io.xlibb.mqtt.utils.MqttConstants.CRYPTO_TRUSTSTORE_PATH;
import static io.xlibb.mqtt.utils.MqttConstants.DEFAULT_TLS_PROTOCOL;
import static io.xlibb.mqtt.utils.MqttConstants.ERROR_NAME;
import static io.xlibb.mqtt.utils.MqttConstants.KEEP_ALIVE_INTERVAL;
import static io.xlibb.mqtt.utils.MqttConstants.KEY;
import static io.xlibb.mqtt.utils.MqttConstants.KEY_FILE;
import static io.xlibb.mqtt.utils.MqttConstants.KEY_PASSWORD;
import static io.xlibb.mqtt.utils.MqttConstants.KEY_STORE_PASSWORD;
import static io.xlibb.mqtt.utils.MqttConstants.KEY_STORE_PATH;
import static io.xlibb.mqtt.utils.MqttConstants.MAX_RECONNECT_DELAY;
import static io.xlibb.mqtt.utils.MqttConstants.NATIVE_DATA_PRIVATE_KEY;
import static io.xlibb.mqtt.utils.MqttConstants.NATIVE_DATA_PUBLIC_KEY_CERTIFICATE;
import static io.xlibb.mqtt.utils.MqttConstants.PASSWORD;
import static io.xlibb.mqtt.utils.MqttConstants.PROTOCOL_NAME;
import static io.xlibb.mqtt.utils.MqttConstants.PROTOCOL_VERSION;
import static io.xlibb.mqtt.utils.MqttConstants.SECURE_SOCKET;
import static io.xlibb.mqtt.utils.MqttConstants.SERVER_URIS;
import static io.xlibb.mqtt.utils.MqttConstants.USERNAME;

/**
 * Class containing the utility functions related to the clients.
 */
public class MqttUtils {

    public static MqttConnectionOptions getMqttConnectOptions(BMap<BString, Object> configuration) {
        MqttConnectionOptions options = new MqttConnectionOptions();
        Object connectionConfigObject = configuration.get(CONNECTION_CONFIGURATION);
        if (connectionConfigObject != null && connectionConfigObject instanceof BMap) {
            BMap<BString, Object> connectionConfig = (BMap<BString, Object>) connectionConfigObject;
            Object username = connectionConfig.get(USERNAME);
            if (username != null) {
                options.setUserName(((BString) username).getValue());
            }
            Object password = connectionConfig.get(PASSWORD);
            if (password != null) {
                options.setPassword(((BString) password).getValue().getBytes(StandardCharsets.UTF_8));
            }
            Object maxReconnectDelay = connectionConfig.get(MAX_RECONNECT_DELAY);
            if (maxReconnectDelay != null) {
                options.setMaxReconnectDelay(((Long) maxReconnectDelay).intValue());
            }
            Object keepAliveInterval = connectionConfig.get(KEEP_ALIVE_INTERVAL);
            if (keepAliveInterval != null) {
                options.setKeepAliveInterval(((Long) keepAliveInterval).intValue());
            }
            Object connectionTimeout = connectionConfig.get(CONNECTION_TIMEOUT);
            if (connectionTimeout != null) {
                options.setConnectionTimeout(((Long) connectionTimeout).intValue());
            }
            Object cleanStart = connectionConfig.get(CLEAN_START);
            if (cleanStart != null) {
                options.setCleanStart((boolean) cleanStart);
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
        Object bCert = secureSocket.get(CERT);
        BMap<BString, BString> keyRecord = (BMap<BString, BString>) secureSocket.getMapValue(KEY);
        BMap protocol = secureSocket.getMapValue(PROTOCOL_NAME);
        String contextProtocol = DEFAULT_TLS_PROTOCOL;
        KeyManagerFactory kmf = null;
        TrustManagerFactory tmf;
        if (Objects.nonNull(protocol)) {
            String version = protocol.getStringValue(PROTOCOL_VERSION).getValue();
            String protocolName = protocol.getStringValue(PROTOCOL_NAME).getValue();
            contextProtocol = protocolName + "v" + version;
        }
        try {
            Security.addProvider(new BouncyCastleProvider());
            if (bCert instanceof BString) {
                tmf = getTrustManagerFactory((BString) bCert);
            } else {
                BMap<BString, BString> trustStore = (BMap<BString, BString>) bCert;
                tmf = getTrustManagerFactory(trustStore);
            }
            if (keyRecord != null) {
                if (keyRecord.containsKey(CERT_FILE)) {
                    BString certFile = keyRecord.get(CERT_FILE);
                    BString keyFile = keyRecord.get(KEY_FILE);
                    BString keyPassword = keyRecord.getStringValue(KEY_PASSWORD);
                    kmf = getKeyManagerFactory(certFile, keyFile, keyPassword);
                } else {
                    kmf = getKeyManagerFactory(keyRecord);
                }
            }
            SSLContext sslContext = SSLContext.getInstance(contextProtocol);
            if (Objects.nonNull(kmf)) {
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            } else {
                sslContext.init(null, tmf.getTrustManagers(), null);
            }
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw createMqttError(e);
        }
    }

    private static KeyManagerFactory getKeyManagerFactory(BMap<BString, BString> keyStore) throws Exception {
        BString keyStorePath = keyStore.getStringValue(KEY_STORE_PATH);
        BString keyStorePassword = keyStore.getStringValue(KEY_STORE_PASSWORD);
        KeyStore ks = getKeyStore(keyStorePath, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyStorePassword.getValue().toCharArray());
        return kmf;
    }

    private static KeyManagerFactory getKeyManagerFactory(BString certFile, BString keyFile, BString keyPassword)
            throws Exception {
        Object publicKey = Decode.decodeRsaPublicKeyFromCertFile(certFile);
        if (publicKey instanceof BMap) {
            X509Certificate publicCert = (X509Certificate) ((BMap<BString, Object>) publicKey).getNativeData(
                    NATIVE_DATA_PUBLIC_KEY_CERTIFICATE);
            Object privateKeyMap = Decode.decodeRsaPrivateKeyFromKeyFile(keyFile, keyPassword);
            if (privateKeyMap instanceof BMap) {
                PrivateKey privateKey = (PrivateKey) ((BMap<BString, Object>) privateKeyMap).getNativeData(
                        NATIVE_DATA_PRIVATE_KEY);
                KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                ks.load(null, "".toCharArray());
                ks.setKeyEntry(UUID.randomUUID().toString(), privateKey, "".toCharArray(),
                        new X509Certificate[]{publicCert});
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, "".toCharArray());
                return kmf;
            } else {
                throw new Exception("Failed to get the private key from Crypto API. " +
                        ((BError) privateKeyMap).getErrorMessage().getValue());
            }
        } else {
            throw new Exception("Failed to get the public key from Crypto API. " +
                    ((BError) publicKey).getErrorMessage().getValue());
        }
    }

    private static TrustManagerFactory getTrustManagerFactory(BString cert) throws Exception {
        Object publicKeyMap = Decode.decodeRsaPublicKeyFromCertFile(cert);
        if (publicKeyMap instanceof BMap) {
            X509Certificate x509Certificate = (X509Certificate) ((BMap<BString, Object>) publicKeyMap)
                    .getNativeData(NATIVE_DATA_PUBLIC_KEY_CERTIFICATE);
            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(null, "".toCharArray());
            ts.setCertificateEntry(UUID.randomUUID().toString(), x509Certificate);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            return tmf;
        } else {
            throw new Exception("Failed to get the public key from Crypto API. " +
                    ((BError) publicKeyMap).getErrorMessage().getValue());
        }
    }

    private static TrustManagerFactory getTrustManagerFactory(BMap<BString, BString> trustStore) throws Exception {
        BString trustStorePath = trustStore.getStringValue(CRYPTO_TRUSTSTORE_PATH);
        BString trustStorePassword = trustStore.getStringValue(CRYPTO_TRUSTSTORE_PASSWORD);
        KeyStore ts = getKeyStore(trustStorePath, trustStorePassword);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        return tmf;
    }

    private static KeyStore getKeyStore(BString path, BString password) throws Exception {
        try (FileInputStream is = new FileInputStream(path.getValue())) {
            char[] passphrase = password.getValue().toCharArray();
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, passphrase);
            return ks;
        }
    }

    public static BError createMqttError(Exception exception) {
        Throwable cause = exception.getCause();
        BMap<BString, Object> errorDetailMap = ValueCreator.createRecordValue(getModule(), "ErrorDetails");
        if (exception instanceof MqttException) {
            errorDetailMap.put(StringUtils.fromString("reasonCode"), ((MqttException) exception).getReasonCode());
        }
        if (cause != null) {
            return ErrorCreator.createError(getModule(), ERROR_NAME, StringUtils.fromString(exception.getMessage()),
                    ErrorCreator.createError(exception.getCause()), errorDetailMap);
        }
        return ErrorCreator.createError(getModule(), ERROR_NAME, StringUtils.fromString(exception.getMessage()),
                null, errorDetailMap);
    }
}
