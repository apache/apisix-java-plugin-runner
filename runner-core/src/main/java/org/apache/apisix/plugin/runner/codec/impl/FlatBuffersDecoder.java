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

import io.netty.buffer.ByteBuf;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.codec.PluginRunnerDecoder;

import java.nio.ByteBuffer;

public class FlatBuffersDecoder implements PluginRunnerDecoder {
    
    @Override
    public A6Request decode(ByteBuf buf) {
        final byte type = buf.readByte();
        final short length = buf.readShort();
        
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buf.getBytes(length, buffer);
        buffer.flip();
        
        if (type == 0) {
            return A6ConfigRequest.from(buffer);
        } else {
            return HttpRequest.from(buffer);
        }
    }
}
