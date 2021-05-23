/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.apisix.plugin.runner;

import io.github.api7.A6.HTTPReqCall.Req;
import io.github.api7.A6.TextEntry;
import org.apache.apisix.plugin.runner.filter.PluginFilter;

import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpRequest implements A6Request {

    private final Req req;

    private HttpResponse response;

    private io.github.api7.A6.PrepareConf.Req config;

    private Long requestId;

    private String sourceIP;

    private Method method;

    private String path;

    private Map<String, String> headers;

    private Map<String, String> args;

    public HttpRequest(Req req) {
        this.req = req;
    }

    public String getConfig(PluginFilter filter) {
        for (int i = 0; i < config.confLength(); i++) {
            TextEntry conf = config.conf(i);
            if (conf.name().equals(filter.getClass().getSimpleName())) {
                return conf.value();
            }
        }
        return null;
    }

    public long getRequestId() {
        if (Objects.isNull(requestId)) {
            requestId = req.id();

        }
        return requestId;
    }

    public String getSourceIP() {
        if (Objects.isNull(sourceIP)) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < req.srcIpLength(); i++) {
                builder.append(req.srcIp(i)).append('.');
            }
            sourceIP = builder.substring(0, builder.length() - 1);
        }

        return sourceIP;
    }

    public Method getMethod() {
        if (Objects.isNull(method)) {
            method = Method.values()[req.method()];
        }
        return method;
    }

    public String getPath() {
        if (Objects.isNull(path)) {
            path = req.path();
        }
        return path;
    }

    public void setPath(String path) {
        response.setPath(path);
    }

    public Map<String, String> getHeaders() {
        if (Objects.isNull(headers)) {
            headers = new HashMap<>();
            for (int i = 0; i < req.headersLength(); i++) {
                TextEntry header = req.headers(i);
                headers.put(header.name(), header.value());
            }
        }
        return headers;
    }

    public void setHeader(String headerKey, String headerValue) {
        response.setReqHeader(headerKey, headerValue);
    }

    public Map<String, String> getArgs() {
        if (Objects.isNull(args)) {
            args = new HashMap<>();
            for (int i = 0; i < req.argsLength(); i++) {
                TextEntry arg = req.args(i);
                args.put(arg.name(), arg.value());
            }
        }
        return args;
    }

    public void setArg(String argKey, String argValue) {
        response.setArgs(argKey, argValue);
    }

    public Map getParameterMap() {
        return null;
    }

    public Enumeration getParameters() {
        return null;
    }

    public String[] getParameterValues(String name) {
        return null; // todo
    }

    public long getConfToken() {
        return req.confToken();
    }

    public static HttpRequest from(ByteBuffer buffer) {
        Req req = Req.getRootAsReq(buffer);
        return new HttpRequest(req);
    }

    public void initCtx(HttpResponse response, io.github.api7.A6.PrepareConf.Req config) {
        this.response = response;
        this.config = config;
    }

    @Override
    public byte getType() {
        return 2;
    }

    public enum Method {
        GET,
        HEAD,
        POST,
        PUT,
        DELETE,
        MKCOL,
        COPY,
        MOVE,
        OPTIONS,
        PROPFIND,
        PROPPATCH,
        LOCK,
        UNLOCK,
        PATCH,
        TRACE,
    }
}
