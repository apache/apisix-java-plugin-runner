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

package org.apache.apisix.plugin.runner.handler;

import com.google.flatbuffers.FlatBufferBuilder;
import io.github.api7.A6.Err.Resp;
import org.apache.apisix.plugin.runner.codec.frame.FrameType;

public class ErrHandler implements RequestHandler {
    private static final FrameType TYPE = FrameType.RPC_ERROR;

    private int code = -1;
    private FlatBufferBuilder builder;

    public ErrHandler(int code) {
        this.code = code;
    }

    @Override
    public FlatBufferBuilder handler(FlatBufferBuilder builder) {
        this.builder = builder;
        Resp.startResp(builder);
        Resp.addCode(builder, code == -1 ? 1 : code);
        int orc = Resp.endResp(builder);
        builder.finish(orc);
        return builder;
    }

    @Override
    public FlatBufferBuilder builder() {
        return this.builder;
    }

    @Override
    public FrameType type() {
        return TYPE;
    }
}
