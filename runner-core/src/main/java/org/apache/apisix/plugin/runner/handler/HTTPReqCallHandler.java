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
import io.github.api7.A6.DataEntry;
import io.github.api7.A6.HTTPReqCall.Req;
import io.github.api7.A6.HTTPReqCall.Resp;
import io.github.api7.A6.HTTPReqCall.Rewrite;
import io.github.api7.A6.HTTPReqCall.Stop;
import io.github.api7.A6.TextEntry;
import org.apache.apisix.plugin.runner.A6Config;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.A6Response;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.codec.frame.FrameType;
import org.apache.apisix.plugin.runner.filter.FilterChain;
import org.springframework.util.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.Map;

public class HTTPReqCallHandler implements RequestHandler, Filter {

    private static final FrameType TYPE = FrameType.RPC_HTTP_REQ_CALL;
    private final Table req;
    private FlatBufferBuilder builder;
    private final Cache<Long, io.github.api7.A6.PrepareConf.Req> cache;
    private final FilterChain chain;

    public HTTPReqCallHandler(ByteBuffer body,
                              Cache<Long, io.github.api7.A6.PrepareConf.Req> cache,
                              FilterChain chain) {
        this.req = Req.getRootAsReq(body);
        this.cache = cache;
        this.chain = chain;
    }

    @Override
    public FlatBufferBuilder handler(FlatBufferBuilder builder) {
        this.builder = builder;
        Req req = (Req) this.req;
        A6Config conf = conf(req);
        HttpRequest request = new HttpRequest(req, conf);
        HttpResponse response = new HttpResponse((int) req.id());
//        chain.doFilter(request, response);
        return buildRes(builder, response);
    }

    private A6Config conf(Req req) {
        io.github.api7.A6.PrepareConf.Req conf = cache.getIfPresent(req.confToken());
        if (null == conf) {
            //TODO error handle
        }
        return new A6Config(conf);
    }

    private FlatBufferBuilder buildRes(FlatBufferBuilder builder, HttpResponse response) {

        if (null == response.getActionType()) {
            response.setActionType(A6Response.ActionType.NONE);
        }

        int action = 0;
        if (response.getActionType() == A6Response.ActionType.Rewrite) {
            action = buildRewriteResp(builder, response);
        }

        if (response.getActionType() == A6Response.ActionType.Stop) {
            action = buildStopResp(builder, response);
        }

        Resp.startResp(builder);
        Resp.addAction(builder, action);
        if (null != response.getActionType()) {
            Resp.addActionType(builder, response.getActionType().getType());
        }
        Resp.addId(builder, response.getRequestId());
        builder.finish(Resp.endResp(builder));
        return builder;
    }

    private int buildStopResp(FlatBufferBuilder builder, HttpResponse response) {
        Stop.startStop(builder);
        addHeaders(builder, response);
        addBody(builder, response);
        Stop.addStatus(builder, response.getStatus().code());
        return Stop.endStop(builder);
    }

    private void addBody(FlatBufferBuilder builder, HttpResponse response) {
        if (!CollectionUtils.isEmpty(response.getBody())) {
            byte[] bodyTexts = new byte[response.getBody().size()];
            for (Map.Entry<String, String> arg : response.getBody().entrySet()) {
                int i = -1;
                int key = builder.createString(arg.getKey());
                int value = builder.createString(arg.getValue());
                int text = DataEntry.createDataEntry(builder, key, value);
                bodyTexts[++i] = (byte) text;
            }
            int body = Stop.createBodyVector(builder, bodyTexts);
            Stop.addBody(builder, body);
        }
    }

    private int buildRewriteResp(FlatBufferBuilder builder, HttpResponse response) {
        Rewrite.startRewrite(builder);
        if (null != response.getPath()) {
            int path = builder.createString(response.getPath());
            Rewrite.addPath(builder, path);
        }
        addHeaders(builder, response);
        addArgs(builder, response);
        return Rewrite.endRewrite(builder);
    }

    private void addArgs(FlatBufferBuilder builder, HttpResponse response) {
        if (!CollectionUtils.isEmpty(response.getArgs())) {
            int[] argTexts = new int[response.getArgs().size()];
            for (Map.Entry<String, String> arg : response.getArgs().entrySet()) {
                int i = -1;
                int key = builder.createString(arg.getKey());
                int value = builder.createString(arg.getValue());
                int text = TextEntry.createTextEntry(builder, key, value);
                argTexts[++i] = text;
            }
            int args = Rewrite.createArgsVector(builder, argTexts);
            Rewrite.addArgs(builder, args);
        }
    }

    private void addHeaders(FlatBufferBuilder builder, HttpResponse response) {
        if (!CollectionUtils.isEmpty(response.getHeaders())) {
            int[] headerTexts = new int[response.getHeaders().size()];
            for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                int i = -1;
                int key = builder.createString(header.getKey());
                int value = builder.createString(header.getValue());
                int text = TextEntry.createTextEntry(builder, key, value);
                headerTexts[++i] = text;
            }
            int headers = Rewrite.createHeadersVector(builder, headerTexts);
            Rewrite.addHeaders(builder, headers);
        }
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
