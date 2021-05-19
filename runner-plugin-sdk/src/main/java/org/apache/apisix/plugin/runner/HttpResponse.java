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

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * table Resp {
 * id:uint32;
 * action:Action;
 * }
 */
@Data
public class HttpResponse implements A6Response {

    private final int requestId;

    private ActionType actionType;

    private Map<String, String> headers;

    private Map<String, String> args;

    private String path;

    private Map<String, String> body;

    private HttpResponseStatus status;

    public HttpResponse(int requestId) {
        this.requestId = requestId;
    }

    public void addHeader(String headerKey, String headerValue) {
        if (Objects.isNull(headers)) {
            headers = new HashMap<>();
        }
        headers.put(headerKey, headerValue);
    }

    public void addArg(String argKey, String argValue) {
        if (Objects.isNull(args)) {
            args = new HashMap<>();
        }
        args.put(argKey, argValue);
    }

    public void setPath(String path) {
        this.path = path;
    }
}
