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
import io.github.api7.A6.TextEntry;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6ErrRequest;
import org.apache.apisix.plugin.runner.A6Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(OutputCaptureExtension.class)
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
    void testEmptyData(CapturedOutput capturedOutput) {
        byte[] bytes = new byte[]{};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
        Assertions.assertTrue(capturedOutput.getOut().contains("receive empty data"));
    }

    @Test
    @DisplayName("test unsupported type")
    void testUnsupportedType(CapturedOutput capturedOutput) {
        byte[] bytes = new byte[]{4};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
        Assertions.assertTrue(capturedOutput.getOut().contains("receive unsupported type: 4"));
    }

    @Test
    @DisplayName("test error data length(1)")
    void testErrorDataLength1(CapturedOutput capturedOutput) {
        // data length is greater than actual length
        byte[] bytes = new byte[]{1, 0, 0, 3, 0};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
        Assertions.assertTrue(capturedOutput.getOut().contains("receive error data length"));
    }

    @Test
    @DisplayName("test error data length(2)")
    void testErrorDataLength2(CapturedOutput capturedOutput) {
        // data length equal to 0
        byte[] bytes = new byte[]{1, 0, 0, 0, 0};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
        Assertions.assertTrue(capturedOutput.getOut().contains("receive error data length"));
    }

    @Test
    @DisplayName("test error data length(3)")
    void testErrorDataLength3(CapturedOutput capturedOutput) {
        // wrong data content
        byte[] bytes = new byte[]{1, 0, 0, 1, 0, 1};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6Request result = flatBuffersDecoder.decode(buffer);
        Assertions.assertEquals(Code.BAD_REQUEST, ((A6ErrRequest) result).getCode());
        Assertions.assertTrue(capturedOutput.getOut().contains("receive error data length"));
    }

    @Test
    @DisplayName("test get body")
    void testGetBody() {
        // {"name":"foo", "value":"bar"}
        byte[] bytes = new byte[]{1, 0, 0, 64, 12, 0, 0, 0, 0, 0, 6, 0, 8, 0, 4, 0, 6, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 12, 0, 0, 0, 8, 0, 12, 0, 8, 0, 4, 0, 8, 0, 0, 0, 8, 0, 0, 0, 12, 0, 0, 0, 3, 0, 0, 0, 98, 97, 114, 0, 3, 0, 0, 0, 102, 111, 111, 0};
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
        byte[] bytes = new byte[]{1, 0, 0, 32, 12, 0, 0, 0, 0, 0, 6, 0, 8, 0, 4, 0, 6, 0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 12, 0, 0, 0, 8, 0, 12, 0, 8, 0, 4, 0, 8, 0, 0, 0, 8, 0, 0, 0, 12, 0, 0, 0, 3, 0, 0, 0, 98, 97, 114, 0, 3, 0, 0, 0, 102, 111, 111, 0};
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        A6ConfigRequest configReq = (A6ConfigRequest) flatBuffersDecoder.decode(buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> configReq.getReq().conf(0));
    }
}
