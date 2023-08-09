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

package org.apache.apisix.plugin.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PostResponseTest {

    @Test
    @DisplayName("test encode with set charset")
    void testEncodeWithSetCharset() {
        long requestId = 1L;
        String body = "dummy body";
        Charset charset = StandardCharsets.UTF_16;
        Map<String, List<String>> headers = new HashMap<>();

        PostResponse postResponse = new PostResponse(requestId, headers);
        postResponse.setBody(body);
        postResponse.setCharset(charset);

        ByteBuffer encoded = postResponse.encode();

        assertTrue(Collections.indexOfSubList(byteArrayToList(encoded.array()), byteArrayToList(body.getBytes(charset))) >= 0);
    }

    @Test
    @DisplayName("test encode without set charset")
    void testEncodeWithoutSetCharset() {
        long requestId = 1L;
        String body = "dummy body";
        Charset charset = StandardCharsets.UTF_8;
        Map<String, List<String>> headers = new HashMap<>();

        PostResponse postResponse = new PostResponse(requestId, headers);
        postResponse.setBody(body);

        ByteBuffer encoded = postResponse.encode();

        assertTrue(Collections.indexOfSubList(byteArrayToList(encoded.array()), byteArrayToList(body.getBytes(charset))) >= 0);
    }

    private List<Byte> byteArrayToList(byte[] array) {
        List<Byte> list = new ArrayList<>();
        for (byte b : array) {
            list.add(b);
        }
        return list;
    }
}
