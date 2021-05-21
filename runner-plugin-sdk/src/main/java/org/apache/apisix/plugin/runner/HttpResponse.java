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
import io.github.api7.A6.DataEntry;
import io.github.api7.A6.HTTPReqCall.Resp;
import io.github.api7.A6.HTTPReqCall.Rewrite;
import io.github.api7.A6.HTTPReqCall.Stop;
import io.github.api7.A6.TextEntry;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;
import org.springframework.util.CollectionUtils;

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

    private final long requestId;

    private ActionType actionType;

    private Map<String, String> headers;

    private Map<String, String> args;

    private String path;

    private Map<String, String> body;

    private HttpResponseStatus status;

    private A6ErrResponse errResponse;

    public HttpResponse(long requestId) {
        this.requestId = getRequestId();
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

    @Override
    public ByteBuffer encode() {

        if (null != getErrResponse()) {
            return getErrResponse().encode();
        }

        FlatBufferBuilder builder = new FlatBufferBuilder();

        if (null == getActionType()) {
            setActionType(A6Response.ActionType.NONE);
        }

        int action = 0;
        if (getActionType() == A6Response.ActionType.Rewrite) {
            action = buildRewriteResp(builder);
        }

        if (getActionType() == A6Response.ActionType.Stop) {
            action = buildStopResp(builder);
        }

        Resp.startResp(builder);
        Resp.addAction(builder, action);
        if (null != getActionType()) {
            Resp.addActionType(builder, getActionType().getType());
        }

        Resp.addId(builder, getRequestId());
        builder.finish(Resp.endResp(builder));
        return builder.dataBuffer();
    }

    private int buildStopResp(FlatBufferBuilder builder) {
        Stop.startStop(builder);
        addHeaders(builder);
        addBody(builder);
        Stop.addStatus(builder, getStatus().code());
        return Stop.endStop(builder);
    }

    private void addBody(FlatBufferBuilder builder) {
        if (!CollectionUtils.isEmpty(getBody())) {
            byte[] bodyTexts = new byte[getBody().size()];
            for (Map.Entry<String, String> arg : getBody().entrySet()) {
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

    private int buildRewriteResp(FlatBufferBuilder builder) {
        Rewrite.startRewrite(builder);
        if (null != getPath()) {
            int path = builder.createString(getPath());
            Rewrite.addPath(builder, path);
        }
        addHeaders(builder);
        addArgs(builder);
        return Rewrite.endRewrite(builder);
    }

    private void addArgs(FlatBufferBuilder builder) {
        if (!CollectionUtils.isEmpty(getArgs())) {
            int[] argTexts = new int[getArgs().size()];
            for (Map.Entry<String, String> arg : getArgs().entrySet()) {
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

    private void addHeaders(FlatBufferBuilder builder) {
        if (!CollectionUtils.isEmpty(getHeaders())) {
            int[] headerTexts = new int[getHeaders().size()];
            for (Map.Entry<String, String> header : getHeaders().entrySet()) {
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
    public byte getType() {
        return 2;
    }
}
