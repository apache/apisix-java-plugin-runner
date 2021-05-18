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

import java.nio.ByteBuffer;
import java.util.Map;

public class A6HttpCallResponse implements A6Response {
    
    private final int requestId;
    
    private final Map<String, String> headers;
    
    private final Map<String, String> parameters;
    
    
    public A6HttpCallResponse(int requestId, Map<String, String> parameters, Map<String, String> headers) {
        this.requestId = requestId;
        this.headers = headers;
        this.parameters = parameters;
        
    }
    
    @Override
    public ByteBuffer encode() {
        return null;
    }
}
