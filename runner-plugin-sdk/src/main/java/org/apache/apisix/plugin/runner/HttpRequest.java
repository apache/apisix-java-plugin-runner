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
import org.springframework.util.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpRequest implements A6Request {

    private final Req req;

    private HttpResponse response;

    private Map<String, String> config;

    private Long requestId;

    private String sourceIP;

    private Method method;

    private String path;

    private Map<String, String> headers;

    private Map<String, String> args;

    public HttpRequest(Req req) {
        this.req = req;
    }

    /**
     * Gets current filter config.
     *
     * @param filter the filter
     * @return the config
     */
    public String getConfig(PluginFilter filter) {
        return config.getOrDefault(filter.name(), null);
    }

    public long getRequestId() {
        if (Objects.isNull(requestId)) {
            requestId = req.id();

        }
        return requestId;
    }

    /**
     * Gets source ip.
     *
     * @return the source ip
     */
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

    /**
     * Gets method.
     *
     * @return the method
     */
    public Method getMethod() {
        if (Objects.isNull(method)) {
            method = Method.values()[req.method()];
        }
        return method;
    }

    /**
     * Gets path.
     *
     * @return the path
     */
    public String getPath() {
        if (Objects.isNull(path)) {
            path = req.path();
        }
        return path;
    }

    /**
     * Rewrite path.
     *
     * @param path the path
     */
    public void setPath(String path) {
        response.setPath(path);
    }

    /**
     * Gets all headers.
     * <p>Examples:</p>
     *
     * <pre>
     * {@code
     * request.getHeaders()
     * }
     * </pre>
     * @return the all headers
     */
    public Map<String, String> getHeader() {
        if (Objects.isNull(headers)) {
            headers = new HashMap<>();
            for (int i = 0; i < req.headersLength(); i++) {
                TextEntry header = req.headers(i);
                headers.put(header.name(), header.value());
            }
        }
        return headers;
    }

    /**
     * Gets the specified header
     *
     * <p>Examples:</p>
     *
     * <pre>
     * {@code
     * request.getHeader("Content-Type");
     * }
     * </pre>
     *
     * @param headerName the header name
     * @return the header value or null
     */
    public String getHeader(String headerName) {
        Map<String, String> headers = getHeader();
        if (!CollectionUtils.isEmpty(headers)) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (header.getKey().equals(headerName)) {
                    return header.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Add, rewrite or delete the specified header
     * <p>Examples:</p>
     *
     * <pre>
     * {@code
     *
     * add new header
     * request.setHeader("New-Header", "new header value");
     *
     * overwrite existing header
     * request.setHeader("Accept", "application/json");
     *
     * delete existing header
     * request.setHeader("Accept", null);
     * }
     * </pre>
     * @param headerKey   the header key
     * @param headerValue the header value
     */
    public void setHeader(String headerKey, String headerValue) {
        response.setReqHeader(headerKey, headerValue);
    }

    /**
     * Gets all args.
     *
     * @return the args
     */
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


    /**
     * Gets the specified  arg.
     *
     * <p>Examples:</p>
     *
     * <pre>
     * {@code
     * request.getArg("foo");
     * }
     * </pre>
     *
     * @param argName the arg name
     * @return the arg
     */
    public String getArg(String argName) {
        Map<String, String> args = getArgs();
        if (!CollectionUtils.isEmpty(args)) {
            for (Map.Entry<String, String> arg : args.entrySet()) {
                if (arg.getKey().equals(argName)) {
                    return arg.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Add, rewrite or delete the specified header
     * <p>Examples:</p>
     *
     *
     * <pre>
     * {@code
     *
     * add new arg
     * request.setArg("foo", "bar");
     *
     * overwrite existing arg
     * request.setArg("foo", "bar");
     *
     * delete existing header
     * request.setArg("foo", null);
     * }
     * </pre>
     * @param argKey the arg key
     * @param argValue the arg value
     */
    public void setArg(String argKey, String argValue) {
        response.setArg(argKey, argValue);
    }

    public long getConfToken() {
        return req.confToken();
    }

    public static HttpRequest from(ByteBuffer buffer) {
        Req req = Req.getRootAsReq(buffer);
        return new HttpRequest(req);
    }

    public void initCtx(HttpResponse response, Map<String, String> config) {
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
