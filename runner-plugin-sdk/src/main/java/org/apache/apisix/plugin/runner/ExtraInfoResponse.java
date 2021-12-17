package org.apache.apisix.plugin.runner;

import io.github.api7.A6.ExtraInfo.Resp;
import lombok.Getter;

import java.nio.ByteBuffer;

public class ExtraInfoResponse implements A6Request {
    @Getter
    private final Resp resp;

    public ExtraInfoResponse(Resp resp) {
        this.resp = resp;
    }

    public static ExtraInfoResponse from(ByteBuffer buffer) {
        Resp req = Resp.getRootAsResp(buffer);
        return new ExtraInfoResponse(req);
    }

    public String getResult() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.resp.resultLength(); i++) {
            builder.append((char) this.resp.result(i));
        }
        return builder.toString();
    }

    @Override
    public byte getType() {
        return 3;
    }
}
