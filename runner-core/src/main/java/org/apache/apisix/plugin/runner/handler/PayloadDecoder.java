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

import io.github.api7.A6.Err.Code;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6ErrRequest;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.ExtraInfoResponse;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.PostRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.apache.apisix.plugin.runner.constants.Constants;

public class PayloadDecoder extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(PayloadDecoder.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        ByteBuffer buffer = byteBuf.nioBuffer();
        A6Request request = decode(buffer);
        ctx.fireChannelRead(request);
    }

    public A6Request decode(ByteBuffer buffer) {
        byte type;
        try {
            type = buffer.get();
        } catch (BufferUnderflowException e) {
            logger.warn("receive empty data");
            return new A6ErrRequest(Code.BAD_REQUEST);
        }

        ByteBuffer body;
        switch (type) {
            case Constants.RPC_PREPARE_CONF:
                A6ConfigRequest a6ConfigRequest;
                try {
                    body = getBody(buffer);
                    a6ConfigRequest = A6ConfigRequest.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    logger.warn("receive error data length");
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return a6ConfigRequest;
            case Constants.RPC_HTTP_REQ_CALL:
                HttpRequest httpRequest;
                try {
                    body = getBody(buffer);
                    httpRequest = HttpRequest.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return httpRequest;

            case Constants.RPC_EXTRA_INFO:
                ExtraInfoResponse extraInfoResponse;
                try {
                    body = getBody(buffer);
                    extraInfoResponse = ExtraInfoResponse.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return extraInfoResponse;

            case Constants.RPC_HTTP_RESP_CALL:
                PostRequest postRequest;
                try {
                    body = getBody(buffer);
                    postRequest = PostRequest.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return postRequest;

            default:
                break;
        }

        logger.warn("receive unsupported type: {}", type);
        return new A6ErrRequest(Code.BAD_REQUEST);
    }

    private int getDataLength(ByteBuffer payload) {
        byte[] bytes = new byte[3];
        for (int i = 0; i < 3; i++) {
            bytes[i] = payload.get();
        }
        return bytes2Int(bytes, 0, 3);
    }

    private ByteBuffer getBody(ByteBuffer payload) throws BufferUnderflowException, IndexOutOfBoundsException {
        int length = getDataLength(payload);
        ByteBuffer buffer = payload.slice();
        byte[] dst = new byte[length];
        buffer.get(dst, 0, length);
        buffer.flip();
        return buffer;
    }

    private int bytes2Int(byte[] b, int start, int len) {
        int sum = 0;
        int end = start + len;
        for (int i = start; i < end; i++) {
            int n = b[i] & 0xff;
            n <<= (--len) * 8;
            sum += n;
        }
        return sum;
    }
}
