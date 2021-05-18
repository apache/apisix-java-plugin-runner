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
import io.github.api7.A6.HTTPReqCall.Req;
import io.github.api7.A6.HTTPReqCall.Resp;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.A6Response;
import org.apache.apisix.plugin.runner.codec.frame.FrameType;

import java.nio.ByteBuffer;

public class HTTPReqCallHandler implements RequestHandler, Filter {

    private static final FrameType TYPE = FrameType.RPC_HTTP_REQ_CALL;
    private final Table req;
    private FlatBufferBuilder builder;
    private final Cache<Long, io.github.api7.A6.PrepareConf.Req> cache;

    public HTTPReqCallHandler(ByteBuffer body, Cache<Long, io.github.api7.A6.PrepareConf.Req> cache) {
        this.req = Req.getRootAsReq(body);
        this.cache = cache;
    }

    @Override
    public FlatBufferBuilder handler(FlatBufferBuilder builder) {
        this.builder = builder;
        Req req = (Req) this.req;

        io.github.api7.A6.PrepareConf.Req conf = cache.getIfPresent(req.confToken());


        Resp.startResp(builder);

        //TODO  wrap Req & Resp
        Object resp = filter(null);


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

    @Override
    public A6Response filter(A6Request req) {
        return null;
    }
}
