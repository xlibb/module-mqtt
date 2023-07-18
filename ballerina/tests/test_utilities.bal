
const NO_AUTH_ENDPOINT = "tcp://localhost:1883";
const AUTH_ONLY_ENDPOINT = "tcp://localhost:1884";
const NO_AUTH_ENCRYPTED_ENDPOINT = "ssl://localhost:8883";
const NO_AUTH_MTLS_ENDPOINT = "ssl://localhost:8884";
const NO_AUTH_EXPIRED_ENDPOINT = "ssl://localhost:8887";
const AUTH_MTLS_ENDPOINT = "ssl://localhost:8888";

const AUTH_USERNAME = "ballerina";
const AUTH_PASSWORD = "ballerinamqtt";

const INVALID_USERNAME = "mqttuser";
const INVALID_PASSWORD = "password";

const SERVER_CERT_PATH = "tests/resources/certsandkeys/server.crt";
const CLIENT_CERT_PATH = "tests/resources/certsandkeys/client.crt";
const CLIENT_KEY_PATH = "tests/resources/certsandkeys/client.key";
const KEY_PASSWORD = "ballerina";

const TRUSTSTORE_PATH = "tests/resources/certsandkeys/ballerinaTruststore.p12";
const TRUSTSTORE_PASSWORD = "ballerina";

const KEYSTORE_PATH = "tests/resources/certsandkeys/ballerinaKeystore.p12";
const KEYSTORE_PASSWORD = "ballerina";

final ConnectionConfiguration authMtlsConnConfig = {
    username: AUTH_USERNAME,
    password: AUTH_PASSWORD,
    secureSocket: {
        cert: SERVER_CERT_PATH,
        key: {
            certFile: CLIENT_CERT_PATH,
            keyFile: CLIENT_KEY_PATH,
            keyPassword: KEY_PASSWORD
        }
    }
};
