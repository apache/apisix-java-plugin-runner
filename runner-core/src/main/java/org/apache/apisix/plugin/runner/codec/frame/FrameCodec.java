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

package org.apache.apisix.plugin.runner.codec.frame;

import java.nio.ByteBuffer;

public class FrameCodec {

    public static int getDataLength(ByteBuffer payload) {
        byte[] bytes = new byte[3];
        for (int i = 0; i < 3; i++) {
            bytes[i] = payload.get();
        }
        return byte3ToInt(bytes);
    }

    public static ByteBuffer getBody(ByteBuffer payload) {
        int length = getDataLength(payload);
        ByteBuffer buffer = payload.slice();
        byte[] dst = new byte[length];
        buffer.get(dst, 0, length);
        buffer.flip();
        return buffer;
    }

    public static ByteBuffer setBody(ByteBuffer payload, byte type) {
        byte[] data = new byte[payload.remaining()];
        payload.get(data);
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
        buffer.put(type);
        // data length
        byte[] length = intToByte3(data.length);
        buffer.put(length);
        // data
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    private static byte[] intToByte3(int i) {
        byte[] targets = new byte[3];
        targets[2] = (byte) (i & 0xFF);
        targets[1] = (byte) (i >> 8 & 0xFF);
        targets[0] = (byte) ((i >> 16 & 0xFF));
        return targets;
    }

    private static int byte3ToInt(byte[] bytes) {
        return bytes[2] & 0xFF |
                (bytes[1] & 0xFF << 8) |
                (bytes[0] & 0xFF << 16);
    }
}
