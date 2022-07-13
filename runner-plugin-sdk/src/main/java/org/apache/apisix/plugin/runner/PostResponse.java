package org.apache.apisix.plugin.runner;

import com.google.flatbuffers.FlatBufferBuilder;
import io.github.api7.A6.HTTPRespCall.Resp;
import io.github.api7.A6.TextEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PostResponse implements A6Response {

    private final Logger logger = LoggerFactory.getLogger(PostResponse.class);

    private final long requestId;

    private String body;

    private Integer statusCode;

    private Map<String, String> headers;

    public PostResponse(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public ByteBuffer encode() {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        int bodyIndex = -1;
        if (StringUtils.hasText(body)) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            bodyIndex = Resp.createBodyVector(builder, bodyBytes);
        }

        int headerIndex = -1;
        if (!CollectionUtils.isEmpty(headers)) {
            int[] headerTexts = new int[headers.size()];
            int i = -1;
            for (Map.Entry<String, String> header : headers.entrySet()) {
                int key = builder.createString(header.getKey());
                int value = 0;
                if (!Objects.isNull(header.getValue())) {
                    value = builder.createString(header.getValue());
                }
                int text = TextEntry.createTextEntry(builder, key, value);
                headerTexts[++i] = text;
            }
            headerIndex = Resp.createHeadersVector(builder, headerTexts);
        }

        Resp.startResp(builder);
        Resp.addId(builder, this.requestId);

        if (-1 != bodyIndex) {
            Resp.addBody(builder, bodyIndex);

        }

        if (-1 != headerIndex) {
            Resp.addHeaders(builder, headerIndex);
        }

        if (!Objects.isNull(statusCode)) {
            Resp.addStatus(builder, this.statusCode);
        }

        builder.finish(Resp.endResp(builder));
        return builder.dataBuffer();
    }

    @Override
    public byte getType() {
        return 4;
    }

    @Override
    public A6ErrResponse getErrResponse() {
        return A6Response.super.getErrResponse();
    }

    public void setStatusCode(Integer code) {
    }

    public void setHeader(String headerKey, String headerValue) {
        if (headerKey == null) {
            logger.warn("headerKey is null, ignore it");
            return;
        }

        if (Objects.isNull(headers)) {
            headers = new HashMap<>();
        }
        headers.put(headerKey, headerValue);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
