package org.apache.apisix.plugin.runner;

import io.github.api7.A6.HTTPRespCall.Req;
import io.github.api7.A6.TextEntry;
import org.apache.apisix.plugin.runner.filter.PluginFilter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PostRequest implements A6Request {
    private final Req req;

    private Long requestId;

    private Map<String, String> config;

    private Map<String, String> headers;

    private Integer status;

    public PostRequest(Req req) {
        this.req = req;
    }

    public static PostRequest from(ByteBuffer body) {
        Req req = Req.getRootAsReq(body);
        return new PostRequest(req);
    }

    @Override
    public byte getType() {
        return 4;
    }

    public long getConfToken() {
        return req.confToken();
    }

    public long getRequestId() {
        if (Objects.isNull(requestId)) {
            requestId = req.id();
        }
        return requestId;
    }

    public void initCtx(Map<String, String> config) {
        this.config = config;
    }

    public String getConfig(PluginFilter filter) {
        return config.getOrDefault(filter.name(), null);
    }

    public Map<String, String> getUpstreamHeaders() {
        if (Objects.isNull(headers)) {
            headers = new HashMap<>();
            for (int i = 0; i < req.headersLength(); i++) {
                TextEntry header = req.headers(i);
                headers.put(header.name(), header.value());
            }
        }
        return headers;
    }

    public Integer getUpstreamStatusCode() {
        if (Objects.isNull(status)) {
            status = req.status();
        }
        return status;
    }
}
