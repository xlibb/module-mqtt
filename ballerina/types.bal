public type Message record {|
    byte[] payload;
    int qos = 1;
    boolean retained = false;
    boolean duplicate?;
    int messageId?;
|};

public type Service distinct service object {};
