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

import com.google.common.cache.Cache;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import io.github.api7.A6.PrepareConf.Req;
import io.github.api7.A6.PrepareConf.Resp;
import org.apache.apisix.plugin.runner.codec.frame.FrameType;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class PrepareConfHandler implements RequestHandler {
    private static final FrameType TYPE = FrameType.RPC_PREPARE_CONF;
    private final Table req;

    private final Cache<Long, Req> cache;

    public PrepareConfHandler(ByteBuffer body, Cache<Long, Req> cache) {
        this.req = Req.getRootAsReq(body);
        this.cache = cache;
    }

    private FlatBufferBuilder builder;

    @Override
    public FlatBufferBuilder handler(FlatBufferBuilder builder) {
        this.builder = builder;
        int token = confToken();
        Resp.startResp(builder);
        Resp.addConfToken(builder, token);
        builder.finish(Resp.endResp(builder));
        return builder;
    }

    private int confToken() {
        Req req = (Req) this.req;
        int token = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        cache.put((long) token, req);
        return token;
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
