package org.apache.apisix.plugin.runner.codec.frame;

public enum FrameType {
    RPC_ERROR((byte) 0),

    RPC_PREPARE_CONF((byte)1),

    RPC_HTTP_REQ_CALL((byte)2);

    private final byte type;

    FrameType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }
}
