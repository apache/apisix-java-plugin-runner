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

package org.apache.apisix.plugin.runner.codec.impl;

import io.github.api7.A6.Err.Code;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6ErrRequest;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.codec.PluginRunnerDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class FlatBuffersDecoder implements PluginRunnerDecoder {

    private final Logger logger = LoggerFactory.getLogger(FlatBuffersDecoder.class);

    @Override
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
            case 1:
                A6ConfigRequest a6ConfigRequest;
                try {
                    body = getBody(buffer);
                    a6ConfigRequest = A6ConfigRequest.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    logger.warn("receive error data length");
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return a6ConfigRequest;
            case 2:
                HttpRequest httpRequest;
                try {
                    body = getBody(buffer);
                    httpRequest = HttpRequest.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return httpRequest;
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
        return byte3ToInt(bytes);
    }

    private ByteBuffer getBody(ByteBuffer payload) throws BufferUnderflowException, IndexOutOfBoundsException {
        int length = getDataLength(payload);
        ByteBuffer buffer = payload.slice();
        byte[] dst = new byte[length];
        buffer.get(dst, 0, length);
        buffer.flip();
        return buffer;
    }

    private int byte3ToInt(byte[] bytes) {
        return bytes[2] & 0xFF |
                (bytes[1] & 0xFF << 8) |
                (bytes[0] & 0xFF << 16);
    }
}
