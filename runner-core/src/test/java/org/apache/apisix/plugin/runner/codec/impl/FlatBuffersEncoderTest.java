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
import io.github.api7.A6.HTTPReqCall.Action;
import io.github.api7.A6.HTTPReqCall.Rewrite;
import io.github.api7.A6.HTTPReqCall.Stop;
import org.apache.apisix.plugin.runner.A6ConfigResponse;
import org.apache.apisix.plugin.runner.A6ErrResponse;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

@DisplayName("test encode data")
class FlatBuffersEncoderTest {
    FlatBuffersEncoder flatBuffersEncoder = new FlatBuffersEncoder();

    @Test
    @DisplayName("test encode error response(1)")
    void testErrResponseEncode1() {
        A6ErrResponse errResponse = new A6ErrResponse(Code.BAD_REQUEST);
        ByteBuffer result = flatBuffersEncoder.encode(errResponse);
        byte[] res = new byte[result.remaining()];
        result.get(res);

        //example
        byte[] example = new byte[]{0, 0, 0, 12, 8, 0, 0, 0, 4, 0, 4, 0, 4, 0, 0, 0};
        Assertions.assertArrayEquals(res, example);
    }

    @Test
    @DisplayName("test encode error response(2)")
    void testErrResponseEncode2() {
        A6ErrResponse errResponse = new A6ErrResponse(Code.BAD_REQUEST);
        ByteBuffer result = flatBuffersEncoder.encode(errResponse);
        result.position(4);
        io.github.api7.A6.Err.Resp resp = io.github.api7.A6.Err.Resp.getRootAsResp(result);
        Assertions.assertEquals(Code.BAD_REQUEST, resp.code());
    }

    @Test
    @DisplayName("test prepare conf response(1)")
    void testPrepareConfResponseEncode1() {
        A6ConfigResponse configResponse = new A6ConfigResponse(0L);
        ByteBuffer result = flatBuffersEncoder.encode(configResponse);
        byte[] res = new byte[result.remaining()];
        result.get(res);

        //example
        byte[] example = new byte[]{1, 0, 0, 12, 8, 0, 0, 0, 4, 0, 4, 0, 4, 0, 0, 0};
        Assertions.assertArrayEquals(res, example);
    }

    @Test
    @DisplayName("test prepare conf response(2)")
    void testPrepareConfResponseEncode2() {
        A6ConfigResponse configResponse = new A6ConfigResponse(0L);
        ByteBuffer result = flatBuffersEncoder.encode(configResponse);
        result.position(4);
        io.github.api7.A6.PrepareConf.Resp resp = io.github.api7.A6.PrepareConf.Resp.getRootAsResp(result);
        Assertions.assertEquals(resp.confToken(), 0L);
    }

    @Test
    @DisplayName("test http call response")
    void testHTTPCallResponseEncode1() {
        HttpResponse errResponse = new HttpResponse(0L);
        ByteBuffer result = flatBuffersEncoder.encode(errResponse);
        byte[] res = new byte[result.remaining()];
        result.get(res);

        //example
        byte[] example = new byte[]{2, 0, 0, 12, 8, 0, 0, 0, 4, 0, 4, 0, 4, 0, 0, 0};
        Assertions.assertArrayEquals(res, example);
    }

    @Test
    @DisplayName("test http call response, action: none")
    void testHTTPCallResponseEncode2() {
        HttpResponse httpResponse = new HttpResponse(0L);
        ByteBuffer result = flatBuffersEncoder.encode(httpResponse);
        result.position(4);
        io.github.api7.A6.HTTPReqCall.Resp resp = io.github.api7.A6.HTTPReqCall.Resp.getRootAsResp(result);
        Assertions.assertEquals(resp.id(), 0L);
        // default action is none
        Assertions.assertEquals(resp.actionType(), Action.NONE);
    }

    @Test
    @DisplayName("test http call response, action: rewrite")
    void testRewriteResponseEncode() {
        HttpResponse httpResponse = new HttpResponse(0L);
        // set path, args, req header means rewrite request
        httpResponse.setPath("/hello");
        httpResponse.setArg("foo", "bar");
        httpResponse.setReqHeader("Server", "APISIX");
        ByteBuffer result = flatBuffersEncoder.encode(httpResponse);
        result.position(4);
        io.github.api7.A6.HTTPReqCall.Resp resp = io.github.api7.A6.HTTPReqCall.Resp.getRootAsResp(result);
        Assertions.assertEquals(resp.actionType(), Action.Rewrite);
        Rewrite rewrite = (Rewrite) resp.action(new Rewrite());

        Assertions.assertEquals(rewrite.path(), "/hello");
        Assertions.assertEquals(rewrite.args(0).name(), "foo");
        Assertions.assertEquals(rewrite.args(0).value(), "bar");
        Assertions.assertEquals(rewrite.headers(0).name(), "Server");
        Assertions.assertEquals(rewrite.headers(0).value(), "APISIX");
    }

    @Test
    @DisplayName("test http call response, action: stop")
    void testStopResponseEncode() {
        HttpResponse httpResponse = new HttpResponse(0L);
        // set status, body, resp header means stop request
        httpResponse.setStatusCode(401);
        httpResponse.setHeader("code", "401");
        httpResponse.setBody("Unauthorized");
        ByteBuffer result = flatBuffersEncoder.encode(httpResponse);
        result.position(4);
        io.github.api7.A6.HTTPReqCall.Resp resp = io.github.api7.A6.HTTPReqCall.Resp.getRootAsResp(result);
        Assertions.assertEquals(resp.actionType(), Action.Stop);
        Stop stop = (Stop) resp.action(new Stop());

        Assertions.assertEquals(stop.status(), 401);
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < stop.bodyLength(); i++) {
            body.append((char) stop.body(i));
        }
        Assertions.assertEquals(body.toString(), "Unauthorized");
        Assertions.assertEquals(stop.headers(0).name(), "code");
        Assertions.assertEquals(stop.headers(0).value(), "401");
    }

    @Test
    @DisplayName("test mix stop and rewrite response, will use stop response")
    void testMixStopAndRewriteResponseEncode() {
        HttpResponse httpResponse = new HttpResponse(0L);
        // set path, args, req header means rewrite request
        httpResponse.setPath("/hello");
        httpResponse.setArg("foo", "bar");
        httpResponse.setReqHeader("Server", "APISIX");

        // set status, body, resp header means stop request
        httpResponse.setStatusCode(401);
        httpResponse.setHeader("code", "401");
        httpResponse.setBody("Unauthorized");
        ByteBuffer result = flatBuffersEncoder.encode(httpResponse);
        result.position(4);
        io.github.api7.A6.HTTPReqCall.Resp resp = io.github.api7.A6.HTTPReqCall.Resp.getRootAsResp(result);
        Assertions.assertEquals(resp.actionType(), Action.Stop);
    }

    @Test
    @DisplayName("test rewrite more headers and args")
    void testRewriteMoreHeadersAndArgs() {
        HttpResponse httpResponse = new HttpResponse(0L);
        // set path, args, req header means rewrite request
        httpResponse.setPath("/hello");
        httpResponse.setArg("foo", "bar");
        httpResponse.setArg("dog", "cat");
        httpResponse.setArg("abc", "edf");
        httpResponse.setReqHeader("Server", "APISIX");
        httpResponse.setReqHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        httpResponse.setReqHeader("Timestamp", "1623408133");

        ByteBuffer result = flatBuffersEncoder.encode(httpResponse);
        result.position(4);
        io.github.api7.A6.HTTPReqCall.Resp resp = io.github.api7.A6.HTTPReqCall.Resp.getRootAsResp(result);
        Assertions.assertEquals(resp.actionType(), Action.Rewrite);
        Rewrite rewrite = (Rewrite) resp.action(new Rewrite());

        Assertions.assertEquals(rewrite.path(), "/hello");
        Assertions.assertEquals(rewrite.argsLength(), 3);
        for (int i = 0; i < rewrite.argsLength(); i++) {
            if (rewrite.args(i).name().equals("foo")) {
                Assertions.assertEquals(rewrite.args(i).value(), "bar");
            }
            if (rewrite.args(i).name().equals("dog")) {
                Assertions.assertEquals(rewrite.args(i).value(), "cat");
            }
            if (rewrite.args(i).name().equals("abc")) {
                Assertions.assertEquals(rewrite.args(i).value(), "edf");
            }
        }

        Assertions.assertEquals(rewrite.headersLength(), 3);
        for (int i = 0; i < rewrite.headersLength(); i++) {
            if (rewrite.headers(i).name().equals("Server")) {
                Assertions.assertEquals(rewrite.headers(i).value(), "APISIX");
            }
        }
        for (int i = 0; i < rewrite.headersLength(); i++) {
            if (rewrite.headers(i).name().equals("Authorization")) {
                Assertions.assertEquals(rewrite.headers(i).value(), "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
            }
        }
        for (int i = 0; i < rewrite.headersLength(); i++) {
            if (rewrite.headers(i).name().equals("Timestamp")) {
                Assertions.assertEquals(rewrite.headers(i).value(), "1623408133");
            }
        }
    }

    @Test
    @DisplayName("test set more stop headers")
    void testSetMoreStopHeaders() {
        HttpResponse httpResponse = new HttpResponse(0L);
        // set status, body, resp header means stop request
        httpResponse.setHeader("Server", "APISIX");
        httpResponse.setHeader("Error", "Unauthorized");
        httpResponse.setHeader("Timestamp", "1623408133");
        ByteBuffer result = flatBuffersEncoder.encode(httpResponse);
        result.position(4);
        io.github.api7.A6.HTTPReqCall.Resp resp = io.github.api7.A6.HTTPReqCall.Resp.getRootAsResp(result);
        Assertions.assertEquals(resp.actionType(), Action.Stop);
        Stop stop = (Stop) resp.action(new Stop());

        Assertions.assertEquals(stop.headersLength(), 3);
        for (int i = 0; i < stop.headersLength(); i++) {
            if (stop.headers(i).name().equals("Server")) {
                Assertions.assertEquals(stop.headers(i).value(), "APISIX");
            }
            if (stop.headers(i).name().equals("Error")) {
                Assertions.assertEquals(stop.headers(i).value(), "Unauthorized");
            }
            if (stop.headers(i).name().equals("Timestamp")) {
                Assertions.assertEquals(stop.headers(i).value(), "1623408133");
            }
        }
    }
}
