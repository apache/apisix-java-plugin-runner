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
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Map;

// @Readable
public class HttpRequest implements A6Request {

    private final Req req;

    private int id;

    private String sourceIP;

    private Method method;

    private String path;

    private Map<String, String> parameter;

    private Map<String, String> headers;

    private long confToken;

    @Setter
    private A6Config config;

    private Map<String, Object> data;

    public HttpRequest(Req req) {
        this.req = req;
    }

    public long getRequestId() {
        return req.id();
    }

    public String getSourceIP() {
        return ""; // TODO
    }

    public Method getMethod() {
        return Method.values()[req.method()];
    }

    public String getPath() {
        return req.path(); // FiXME
    }

    public String getParameter(String name) {
        return parameter.get(name);
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
