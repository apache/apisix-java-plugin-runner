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

package org.apache.apisix.plugin.runner.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class DelayedDecoder extends LengthFieldBasedFrameDecoder {

    public DelayedDecoder() {
        super(16777215, 0, 0);
    }

    @Override
    protected ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) {
        in.readByte();
        int length = in.readMedium();
        if (in.readableBytes() < length) {
            return null;
        }
        in.readerIndex(0);

        int readLength = in.readableBytes();
        return in.retainedSlice(0, readLength);
    }
}