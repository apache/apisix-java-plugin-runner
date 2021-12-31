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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.apache.apisix.plugin.runner.A6Response;

import java.nio.ByteBuffer;

public class PayloadEncoder extends ChannelOutboundHandlerAdapter {

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof A6Response) {
            A6Response response = (A6Response) msg;
            ByteBuffer buffer = encode(response);
            ByteBuf buf = Unpooled.wrappedBuffer(buffer);
            ctx.write(buf, promise);
        }
    }

    public ByteBuffer encode(A6Response response) {
        ByteBuffer buffer = response.encode();
        return setBody(buffer, response.getType());
    }

    private ByteBuffer setBody(ByteBuffer payload, byte type) {
        byte[] data = new byte[payload.remaining()];
        payload.get(data);
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
        buffer.put(type);
        // data length
        byte[] length = int2Bytes(data.length, 3);
        buffer.put(length);
        // data
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    byte[] int2Bytes(int value, int len) {
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte) ((value >> 8 * i) & 0xff);
        }
        return b;
    }
}
