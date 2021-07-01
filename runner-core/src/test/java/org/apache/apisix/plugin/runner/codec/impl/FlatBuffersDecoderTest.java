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

import com.google.flatbuffers.FlatBufferBuilder;
import io.github.api7.A6.Err.Code;
import io.github.api7.A6.TextEntry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6ErrRequest;
import org.apache.apisix.plugin.runner.A6Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("test decode data")
class FlatBuffersDecoderTest {

    @InjectMocks
    FlatBuffersDecoder flatBuffersDecoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("test empty data")
    void testEmptyData() {
        byte[] bytes = new byte[]{};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
    }

    @Test
    @DisplayName("test unsupported type")
    void testUnsupportedType() {
        byte[] bytes = new byte[]{4};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
    }

    @Test
    @DisplayName("test error data length(1)")
    void testErrorDataLength1() {
        // data length is greater than actual length
        byte[] bytes = new byte[]{1, 0, 0, 3, 0};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
    }

    @Test
    @DisplayName("test error data length(2)")
    void testErrorDataLength2() {
        // data length equal to 0
        byte[] bytes = new byte[]{1, 0, 0, 0, 0};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
    }

    @Test
    @DisplayName("test error data length(3)")
    void testErrorDataLength3() {
        // wrong data content
        byte[] bytes = new byte[]{1, 0, 0, 1, 0, 1};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
    }

    @Test
    @DisplayName("test get body")
    void testGetBody() {
        // mock client assembly data
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int foo = builder.createString("foo");
        int bar = builder.createString("bar");
        int confIndex = TextEntry.createTextEntry(builder, foo, bar);
        int vector = io.github.api7.A6.PrepareConf.Req.createConfVector(builder, new int[]{confIndex});
        io.github.api7.A6.PrepareConf.Req.startReq(builder);
        io.github.api7.A6.PrepareConf.Req.addConf(builder, vector);
        builder.finish(io.github.api7.A6.PrepareConf.Req.endReq(builder));
        byte[] data = new byte[builder.dataBuffer().remaining()];
        builder.dataBuffer().get(data, 0, data.length);
        // use the correct data length
        byte[] header = new byte[]{1, 0, 0, (byte) data.length};
        byte[] bytes = new byte[header.length + data.length];
        // assembly data format
        System.arraycopy(header, 0, bytes, 0, header.length);
        System.arraycopy(data, 0, bytes, header.length, data.length);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6ConfigRequest configReq = (A6ConfigRequest) flatBuffersDecoder.decode(buffer);
        for (int i = 0; i < configReq.getReq().confLength(); i++) {
            TextEntry conf = configReq.getReq().conf(i);
            Assertions.assertEquals("foo", conf.name());
            Assertions.assertEquals("bar", conf.value());
        }
    }

    @Test
    @DisplayName("test get body with error data length")
    void testGetBody2() {
        // {"name":"foo", "value":"bar"}
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int foo = builder.createString("foo");
        int bar = builder.createString("bar");
        int confIndex = TextEntry.createTextEntry(builder, foo, bar);
        int vector = io.github.api7.A6.PrepareConf.Req.createConfVector(builder, new int[]{confIndex});
        io.github.api7.A6.PrepareConf.Req.startReq(builder);
        io.github.api7.A6.PrepareConf.Req.addConf(builder, vector);
        builder.finish(io.github.api7.A6.PrepareConf.Req.endReq(builder));
        byte[] data = new byte[builder.dataBuffer().remaining()];
        builder.dataBuffer().get(data, 0, data.length);
        // se the error data length
        byte errDateLength = (byte) (data.length / 2);
        byte[] header = new byte[]{1, 0, 0, errDateLength};
        byte[] bytes = new byte[header.length + data.length];
        // assembly data format
        System.arraycopy(header, 0, bytes, 0, header.length);
        System.arraycopy(data, 0, bytes, header.length, data.length);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6ConfigRequest configReq = (A6ConfigRequest) flatBuffersDecoder.decode(buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> configReq.getReq().conf(0));
    }

    @Test
    @DisplayName("test decode data length greater then 256")
    void testDecodeDataGreaterLargeThen256() {
        byte[] bytes = new byte[]{0, 1, 4};
        int length = flatBuffersDecoder.bytes2Int(bytes, 0, 3);

        // use Bytebuf getInt function (default 4 bytes) to verify
        ByteBuf buf = Unpooled.buffer(4);
        byte[] bufBytes = {0, 0, 1, 4};
        buf.writeBytes(bufBytes);
        int bufLength = buf.getInt(0);

        Assertions.assertEquals(length, bufLength);
    }
}
