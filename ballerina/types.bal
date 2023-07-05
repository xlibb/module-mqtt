# An MQTT message holds the application payload and options specifying how the message is to be 
# delivered. The message includes a "payload" represented as a byte[].
#
# + payload - Field Description  
# + qos - Quality of Service  
# + retained - Field Description  
# + duplicate - Field Description  
# + messageId - Field Description
public type Message record {|
    byte[] payload;
    int qos = 1;
    boolean retained = false;
    boolean duplicate = false;
    int messageId?;
|};

public type ClientConfiguration record {|
    ConnectionConfiguration connectionConfig?;
|};

public type ListenerConfiguration record {|
    ConnectionConfiguration connectionConfig?;
    boolean manualAcks = false;
|};

public type ConnectionConfiguration record {|
    string username?;
    string password?;
    int maxReconnectDelay?;
    int keepAliveInterval?;
    int maxInflight?;
    int connectionTimeout?;
    boolean cleanSession?;
    string[] serverUris?;
    boolean automaticReconnect?;
|};

public type DeliveryToken record {|
    Message message;
    int[] grantedQos;
    int messageId;
    string[] topics;
|};

# The Mqtt service type.
public type Service distinct service object {};
