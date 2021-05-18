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
import io.github.api7.A6.Err.Code;
import io.github.api7.A6.PrepareConf.Req;
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.codec.frame.FrameCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Properties;

@RequiredArgsConstructor
public class DefaultPayloadHandler implements PayloadHandler {

    private final Logger logger = LoggerFactory.getLogger(DefaultPayloadHandler.class);

    FlatBufferBuilder builder;

    private RequestHandler handler;

    private final Cache<Long, Req> cache;

    public RequestHandler decode(ByteBuffer buffer) {
        byte type = buffer.get();
        ByteBuffer body = FrameCodec.getBody(buffer);

        switch (type) {
            case 1:
                handler = new PrepareConfHandler(body, cache);
                return handler;
            case 2:
                handler = new HTTPReqCallHandler(body, cache);
                return handler;
            default:
                break;
        }

        logger.error("receiving unsupport type: {}", type);
        return error(Code.BAD_REQUEST);
    }

    public ByteBuffer encode(RequestHandler handler) {
        ByteBuffer buffer = this.handler.builder().dataBuffer();
        return FrameCodec.setBody(buffer, this.handler.type());
    }

    @Override
    public RequestHandler error() {
        logger.error("process rpc call error");
        return error(Code.SERVICE_UNAVAILABLE);
    }

    public RequestHandler dispatch(RequestHandler handler) {
        builder = new FlatBufferBuilder();
        builder = this.handler.handler(builder);
        return handler;
    }

    private RequestHandler error(int code) {
        return new ErrHandler(code);
    }

}
