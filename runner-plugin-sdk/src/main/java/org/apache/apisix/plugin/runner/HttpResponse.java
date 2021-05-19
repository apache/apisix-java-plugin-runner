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

import lombok.Data;

import java.nio.ByteBuffer;
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
    
    private Action action;
    
    private Map<String, String> headers;
    
    public HttpResponse(int requestId) {
        this.requestId = requestId;
    }
    
    public void addHeader(String headerKey, String headerValue) {
        if (Objects.isNull(headers)) {
            headers = new HashMap<>();
        }
        headers.put(headerKey, headerValue);
    }
    
    @Override
    public ByteBuffer encode() {
        return null;
    }
}
