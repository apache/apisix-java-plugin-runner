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
import org.apache.apisix.plugin.runner.codec.frame.FrameCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class FlatBuffersDecoder implements PluginRunnerDecoder {

    private final Logger logger = LoggerFactory.getLogger(FlatBuffersDecoder.class);

    @Override
    public A6Request decode(ByteBuffer buffer) {
        byte type = buffer.get();
        ByteBuffer body = FrameCodec.getBody(buffer);
        switch (type) {
            case 1:
                return A6ConfigRequest.from(body);
            case 2:
                return HttpRequest.from(body);
            default:
                break;
        }

        logger.error("receive unsupported type: {}", type);
        return new A6ErrRequest(Code.BAD_REQUEST);
    }
}
