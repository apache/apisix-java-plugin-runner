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

import com.google.flatbuffers.FlatBufferBuilder;
import io.github.api7.A6.PrepareConf.Resp;
import lombok.Getter;

import java.nio.ByteBuffer;

public class A6ConfigResponse implements A6Response {
    
    @Getter
    private final int confToken;
    
    public A6ConfigResponse(int confToken) {
        this.confToken = confToken;
    }
    
    public final ByteBuffer encode() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        Resp.startResp(builder);
        Resp.addConfToken(builder, confToken);
        builder.finish(Resp.endResp(builder));
        return builder.dataBuffer();
    }
}
