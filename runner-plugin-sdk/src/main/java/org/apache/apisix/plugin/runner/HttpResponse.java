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
public class HttpResponse implements A6Response {

    private final long requestId;

    private ActionType actionType;

    private Map<String, String> reqHeaders;

    private Map<String, String> respHeaders;

    private Map<String, String> args;

    private String path;

    private Map<String, String> body;

    private HttpResponseStatus status;

    private A6ErrResponse errResponse;

    public HttpResponse(long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setReqHeader(String headerKey, String headerValue) {
        actionType = ActionType.Rewrite;
        if (Objects.isNull(reqHeaders)) {
            reqHeaders = new HashMap<>();
        }
        reqHeaders.put(headerKey, headerValue);
    }

    public void setArgs(String argKey, String argValue) {
        actionType = ActionType.Rewrite;
        if (Objects.isNull(args)) {
            args = new HashMap<>();
        }
        args.put(argKey, argValue);
    }

    public void setPath(String path) {
        actionType = ActionType.Rewrite;
        this.path = path;
    }

    public void setRespHeaders(String headerKey, String headerValue) {
        actionType = ActionType.Stop;
        if (Objects.isNull(respHeaders)) {
            respHeaders = new HashMap<>();
        }
        respHeaders.put(headerKey, headerValue);
    }

    public void setBody(String bodyKey, String bodyValue) {
        actionType = ActionType.Stop;
        if (Objects.isNull(body)) {
            body = new HashMap<>();
        }
        body.put(bodyKey, bodyValue);
    }

    public void setStatus(HttpResponseStatus status) {
        actionType = ActionType.Stop;
        this.status = status;
    }

    public void setErrResponse(A6ErrResponse errResponse) {
        this.errResponse = errResponse;
    }

    @Override
    public ByteBuffer encode() {
        if (null != errResponse) {
            return errResponse.encode();
        }

        FlatBufferBuilder builder = new FlatBufferBuilder();

        if (Objects.isNull(actionType)) {
            actionType = A6Response.ActionType.NONE;
        }

        int action = 0;
        if (actionType == A6Response.ActionType.Rewrite) {
            action = buildRewriteResp(builder);
        }else if (actionType == A6Response.ActionType.Stop) {
            action = buildStopResp(builder);
        }

        Resp.startResp(builder);
        Resp.addAction(builder, action);
        Resp.addActionType(builder, actionType.getType());
        Resp.addId(builder, getRequestId());
        builder.finish(Resp.endResp(builder));
        return builder.dataBuffer();
    }

    private int buildStopResp(FlatBufferBuilder builder) {
        int headerIndex = -1;
        if (!CollectionUtils.isEmpty(respHeaders)) {
            int[] headerTexts = new int[respHeaders.size()];
            for (Map.Entry<String, String> header : respHeaders.entrySet()) {
                int i = -1;
                int key = builder.createString(header.getKey());
                int value = builder.createString(header.getValue());
                int text = TextEntry.createTextEntry(builder, key, value);
                headerTexts[++i] = text;
            }
            headerIndex = Stop.createHeadersVector(builder, headerTexts);
        }

        int bodyIndex = -1;
        if (!CollectionUtils.isEmpty(body)) {
            byte[] bodyTexts = new byte[body.size()];
            for (Map.Entry<String, String> arg : body.entrySet()) {
                int i = -1;
                int key = builder.createString(arg.getKey());
                int value = builder.createString(arg.getValue());
                int text = DataEntry.createDataEntry(builder, key, value);
                bodyTexts[++i] = (byte) text;
            }
            bodyIndex = Stop.createBodyVector(builder, bodyTexts);
        }

        Stop.startStop(builder);
        if (!Objects.isNull(status)) {
            Stop.addStatus(builder, status.code());
        }
        if (-1 != headerIndex) {
            Stop.addHeaders(builder, headerIndex);
        }
        if (-1 != bodyIndex) {
            Stop.addBody(builder, bodyIndex);
        }
        return Stop.endStop(builder);
    }

    private int buildRewriteResp(FlatBufferBuilder builder) {
        int pathIndex = -1;
        if (Objects.isNull(path)) {
            pathIndex = builder.createString(path);
        }

        int headerIndex = -1;
        if (!CollectionUtils.isEmpty(reqHeaders)) {
            int[] headerTexts = new int[reqHeaders.size()];
            for (Map.Entry<String, String> header : reqHeaders.entrySet()) {
                int i = -1;
                int key = builder.createString(header.getKey());
                int value = builder.createString(header.getValue());
                int text = TextEntry.createTextEntry(builder, key, value);
                headerTexts[++i] = text;
            }
            headerIndex = Rewrite.createHeadersVector(builder, headerTexts);
        }

        int argsIndex = -1;
        if (!CollectionUtils.isEmpty(args)) {
            int[] argTexts = new int[args.size()];
            for (Map.Entry<String, String> arg : args.entrySet()) {
                int i = -1;
                int key = builder.createString(arg.getKey());
                int value = builder.createString(arg.getValue());
                int text = TextEntry.createTextEntry(builder, key, value);
                argTexts[++i] = text;
            }
            argsIndex = Rewrite.createArgsVector(builder, argTexts);
        }
        Rewrite.startRewrite(builder);
        if (-1 != pathIndex) {
            Rewrite.addPath(builder, pathIndex);
        }
        if (-1 != headerIndex) {
            Rewrite.addHeaders(builder, headerIndex);
        }
        if (-1 != argsIndex) {
            Rewrite.addArgs(builder, argsIndex);
        }
        return Rewrite.endRewrite(builder);
    }

    @Override
    public byte getType() {
        return 2;
    }
}
