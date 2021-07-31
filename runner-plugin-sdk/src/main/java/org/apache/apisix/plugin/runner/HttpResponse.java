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
import io.github.api7.A6.HTTPReqCall.Resp;
import io.github.api7.A6.HTTPReqCall.Rewrite;
import io.github.api7.A6.HTTPReqCall.Stop;
import io.github.api7.A6.TextEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
    private final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private final long requestId;

    private ActionType actionType;

    private Map<String, String> reqHeaders;

    private Map<String, String> respHeaders;

    private Map<String, String> args;

    private String path;

    private String body;

    private Integer statusCode;

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

    /**
     * Sets arg.
     *
     * @param argKey   the arg key
     * @param argValue the arg value
     */
    public void setArg(String argKey, String argValue) {
        actionType = ActionType.Rewrite;
        if (Objects.isNull(args)) {
            args = new HashMap<>();
        }
        args.put(argKey, argValue);
    }

    /**
     * Sets path.
     *
     * @param path the path
     */
    public void setPath(String path) {
        actionType = ActionType.Rewrite;
        this.path = path;
    }

    /**
     * Sets header.
     *
     * @param headerKey   the header key
     * @param headerValue the header value
     */
    public void setHeader(String headerKey, String headerValue) {
        actionType = ActionType.Stop;
        if (Objects.isNull(respHeaders)) {
            respHeaders = new HashMap<>();
        }
        respHeaders.put(headerKey, headerValue);
    }

    /**
     * Sets body.
     *
     * @param body the body(string)
     */
    public void setBody(String body) {
        actionType = ActionType.Stop;
        this.body = body;
    }

    /**
     * Sets status code.
     *
     * @param statusCode the status code
     */
    public void setStatusCode(int statusCode) {
        actionType = ActionType.Stop;
        this.statusCode = statusCode;
    }

    public void setErrResponse(A6ErrResponse errResponse) {
        this.errResponse = errResponse;
    }

    @Override
    public A6ErrResponse getErrResponse() {
        return this.errResponse;
    }

    @Override
    public ByteBuffer encode() {
        if (!Objects.isNull(errResponse)) {
            return errResponse.encode();
        }

        FlatBufferBuilder builder = new FlatBufferBuilder();

        if (Objects.isNull(actionType)) {
            actionType = A6Response.ActionType.NONE;
        }

        int action = 0;

        if (actionType == A6Response.ActionType.Rewrite) {
            action = buildRewriteResp(builder);
        } else if (actionType == A6Response.ActionType.Stop) {
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
            int i = -1;
            for (Map.Entry<String, String> header : respHeaders.entrySet()) {
                int key = builder.createString(header.getKey());
                int value = 0;
                if (!Objects.isNull(header.getValue())) {
                    value = builder.createString(header.getValue());
                }
                int text = TextEntry.createTextEntry(builder, key, value);
                headerTexts[++i] = text;
            }
            headerIndex = Stop.createHeadersVector(builder, headerTexts);
        }

        int bodyIndex = -1;
        if (StringUtils.hasText(body)) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            bodyIndex = Stop.createBodyVector(builder, bodyBytes);
        }

        Stop.startStop(builder);
        if (!Objects.isNull(statusCode)) {
            Stop.addStatus(builder, statusCode);
        } else {
            /**
             * Avoid APISIX using 0 as the default HTTP Status Code
             * {@link org.apache.apisix.plugin.runner.HttpResponse#setStatusCode(int statusCode)}
             * @see https://github.com/apache/apisix-java-plugin-runner/issues/55
            */
            Stop.addStatus(builder, 500);
            logger.info("Use 500 as the default HTTP Status Code when setStatusCode is not called");
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
        if (!Objects.isNull(path)) {
            pathIndex = builder.createString(path);
        }

        int headerIndex = -1;
        if (!CollectionUtils.isEmpty(reqHeaders)) {
            int[] headerTexts = new int[reqHeaders.size()];
            int i = -1;
            for (Map.Entry<String, String> header : reqHeaders.entrySet()) {
                int key = builder.createString(header.getKey());
                int value = 0;
                if (!Objects.isNull(header.getValue())) {
                    value = builder.createString(header.getValue());
                }
                int text = TextEntry.createTextEntry(builder, key, value);
                headerTexts[++i] = text;
            }
            headerIndex = Rewrite.createHeadersVector(builder, headerTexts);
        }

        int argsIndex = -1;
        if (!CollectionUtils.isEmpty(args)) {
            int[] argTexts = new int[args.size()];
            int i = -1;
            for (Map.Entry<String, String> arg : args.entrySet()) {
                int key = builder.createString(arg.getKey());
                int value = 0;
                if (!Objects.isNull(arg.getValue())) {
                    value = builder.createString(arg.getValue());
                }
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
