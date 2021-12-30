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
import com.google.common.cache.CacheBuilder;
import com.google.flatbuffers.FlatBufferBuilder;
import io.github.api7.A6.ExtraInfo.Resp;
import io.github.api7.A6.TextEntry;
import io.netty.channel.embedded.EmbeddedChannel;
import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6ConfigResponse;
import org.apache.apisix.plugin.runner.ExtraInfoRequest;
import org.apache.apisix.plugin.runner.ExtraInfoResponse;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("test extra info")
class ExtraInfoTest {
    private PrintStream console = null;
    private ByteArrayOutputStream bytes = null;

    private static final String[] EXTRAINFO_VARS = new String[]{"remote_addr", "server_port", "content_type"};

    HTTPReqCallHandler httpReqCallHandler;

    Cache<Long, A6Conf> cache;

    Map<String, PluginFilter> filters;

    EmbeddedChannel channel;

    PrepareConfHandler prepareConfHandler;

    long confToken;

    @BeforeEach
    void setUp() {

        bytes = new ByteArrayOutputStream();
        console = System.out;
        System.setOut(new PrintStream(bytes));

        filters = new HashMap<>();
        filters.put("FooFilter", new PluginFilter() {
            @Override
            public String name() {
                return "FooFilter";
            }

            @Override
            @SuppressWarnings("unchecked")
            public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
                String remote_addr = request.getVars("remote_addr");
                String server_port = request.getVars("server_port");
                System.out.println("remote_addr: " + remote_addr);
                System.out.println("server_port: " + server_port);
                chain.filter(request, response);
            }

            @Override
            public List<String> requiredVars() {
                return List.of("remote_addr", "server_port");
            }

            @Override
            public Boolean requiredBody() {
                return false;
            }
        });

        filters.put("CatFilter", new PluginFilter() {
            @Override
            public String name() {
                return "CatFilter";
            }

            @Override
            public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
                String body = request.getBody();
                String content_type = request.getVars("content_type");
                System.out.println("content_type: " + content_type);
                System.out.println("body: " + body);
                chain.filter(request, response);
            }

            @Override
            public List<String> requiredVars() {
                return List.of("content_type");
            }

            @Override
            public Boolean requiredBody() {
                return true;
            }
        });
        cache = CacheBuilder.newBuilder().expireAfterWrite(3600, TimeUnit.SECONDS).maximumSize(1000).build();
        FlatBufferBuilder builder = new FlatBufferBuilder();

        int foo = builder.createString("FooFilter");
        int bar = builder.createString("{\"conf_key1\":\"conf_value1\",\"conf_key2\":2}");
        int filter1 = TextEntry.createTextEntry(builder, foo, bar);

        int cat = builder.createString("CatFilter");
        int dog = builder.createString("Dog");
        int filter2 = TextEntry.createTextEntry(builder, cat, dog);

        int confVector = io.github.api7.A6.PrepareConf.Req.createConfVector(builder, new int[]{filter1, filter2});
        io.github.api7.A6.PrepareConf.Req.startReq(builder);
        io.github.api7.A6.PrepareConf.Req.addConf(builder, confVector);
        builder.finish(io.github.api7.A6.PrepareConf.Req.endReq(builder));
        io.github.api7.A6.PrepareConf.Req req = io.github.api7.A6.PrepareConf.Req.getRootAsReq(builder.dataBuffer());

        A6ConfigRequest request = new A6ConfigRequest(req);
        prepareConfHandler = new PrepareConfHandler(cache, filters);
        channel = new EmbeddedChannel(new BinaryProtocolDecoder(), prepareConfHandler);
        channel.writeInbound(request);
        channel.finish();
        A6ConfigResponse response = channel.readOutbound();
        confToken = response.getConfToken();

        prepareConfHandler = new PrepareConfHandler(cache, filters);
        httpReqCallHandler = new HTTPReqCallHandler(cache);
        channel = new EmbeddedChannel(new BinaryProtocolDecoder(), prepareConfHandler, httpReqCallHandler);
    }

    @AfterEach
    void setDown() {
        System.setOut(console);
    }

    @Test
    @DisplayName("test fetch nginx vars of extra info")
    void testFetchVars() {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        io.github.api7.A6.HTTPReqCall.Req.startReq(builder);
        io.github.api7.A6.HTTPReqCall.Req.addConfToken(builder, confToken);
        builder.finish(io.github.api7.A6.HTTPReqCall.Req.endReq(builder));

        io.github.api7.A6.HTTPReqCall.Req req = io.github.api7.A6.HTTPReqCall.Req.getRootAsReq(builder.dataBuffer());
        HttpRequest request = new HttpRequest(req);
        channel.writeInbound(request);
        channel.finish();
        ExtraInfoRequest eir1 = channel.readOutbound();
        ExtraInfoRequest eir2 = channel.readOutbound();
        ExtraInfoRequest eir3 = channel.readOutbound();
        assertThat(EXTRAINFO_VARS).contains((String) ReflectionTestUtils.getField(eir1, "var"));
        assertThat(EXTRAINFO_VARS).contains((String) ReflectionTestUtils.getField(eir2, "var"));
        assertThat(EXTRAINFO_VARS).contains((String) ReflectionTestUtils.getField(eir3, "var"));
    }

    @Test
    @DisplayName("test fetch request body of extra info")
    void testFetchBody() {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        io.github.api7.A6.HTTPReqCall.Req.startReq(builder);
        io.github.api7.A6.HTTPReqCall.Req.addConfToken(builder, confToken);
        builder.finish(io.github.api7.A6.HTTPReqCall.Req.endReq(builder));

        io.github.api7.A6.HTTPReqCall.Req req = io.github.api7.A6.HTTPReqCall.Req.getRootAsReq(builder.dataBuffer());
        HttpRequest request = new HttpRequest(req);
        channel.writeInbound(request);
        channel.finish();
        channel.readOutbound();
        channel.readOutbound();
        channel.readOutbound();
        ExtraInfoRequest exr = channel.readOutbound();
        Assertions.assertEquals(true, ReflectionTestUtils.getField(exr, "reqBody"));
    }

    @Test
    @DisplayName("test fetch request body of extra info")
    void testGetVarsInPluginFilter() {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        io.github.api7.A6.HTTPReqCall.Req.startReq(builder);
        io.github.api7.A6.HTTPReqCall.Req.addConfToken(builder, confToken);
        builder.finish(io.github.api7.A6.HTTPReqCall.Req.endReq(builder));

        io.github.api7.A6.HTTPReqCall.Req req = io.github.api7.A6.HTTPReqCall.Req.getRootAsReq(builder.dataBuffer());
        HttpRequest request = new HttpRequest(req);
        channel.writeInbound(request);
        channel.flushInbound();

        for (int i = 0; i < 4; i++) {
            ExtraInfoRequest eir = channel.readOutbound();
            if (ReflectionTestUtils.getField(eir, "var") != null && "remote_addr" == ReflectionTestUtils.getField(eir, "var")) {
                builder.clear();
                int res1 = builder.createString("127.0.0.1");
                io.github.api7.A6.ExtraInfo.Resp.startResp(builder);
                io.github.api7.A6.ExtraInfo.Resp.addResult(builder, res1);
                int resp1 = Resp.endResp(builder);
                builder.finish(resp1);
                ExtraInfoResponse extraInfoResponse1 = ExtraInfoResponse.from(builder.dataBuffer());
                channel.writeInbound(extraInfoResponse1);
                channel.flushInbound();
                continue;
            }

            if (ReflectionTestUtils.getField(eir, "var") != null && "server_port" == ReflectionTestUtils.getField(eir, "var")) {
                builder.clear();
                int res2 = builder.createString("9080");
                io.github.api7.A6.ExtraInfo.Resp.startResp(builder);
                io.github.api7.A6.ExtraInfo.Resp.addResult(builder, res2);
                int resp2 = Resp.endResp(builder);
                builder.finish(resp2);
                ExtraInfoResponse extraInfoResponse2 = ExtraInfoResponse.from(builder.dataBuffer());
                channel.writeInbound(extraInfoResponse2);
                channel.flushInbound();
                continue;
            }

            if (ReflectionTestUtils.getField(eir, "var") != null && "content_type" == ReflectionTestUtils.getField(eir, "var")) {
                builder.clear();
                int res3 = builder.createString("application/json");
                io.github.api7.A6.ExtraInfo.Resp.startResp(builder);
                io.github.api7.A6.ExtraInfo.Resp.addResult(builder, res3);
                int resp3 = Resp.endResp(builder);
                builder.finish(resp3);
                ExtraInfoResponse extraInfoResponse3 = ExtraInfoResponse.from(builder.dataBuffer());
                channel.writeInbound(extraInfoResponse3);
                channel.flushInbound();
                continue;
            }

            if ((ReflectionTestUtils.getField(eir, "reqBody") == null) || !((Boolean) ReflectionTestUtils.getField(eir, "reqBody"))) {
                continue;
            }
            builder.clear();
            int res4 = builder.createString("abcd");
            io.github.api7.A6.ExtraInfo.Resp.startResp(builder);
            io.github.api7.A6.ExtraInfo.Resp.addResult(builder, res4);
            int resp4 = Resp.endResp(builder);
            builder.finish(resp4);
            ExtraInfoResponse extraInfoResponse4 = ExtraInfoResponse.from(builder.dataBuffer());
            channel.writeInbound(extraInfoResponse4);
            channel.flushInbound();
        }

        Assertions.assertTrue(bytes.toString().contains("remote_addr: 127.0.0.1"));
        Assertions.assertTrue(bytes.toString().contains("server_port: 9080"));
        Assertions.assertTrue(bytes.toString().contains("content_type: application/json"));
        Assertions.assertTrue(bytes.toString().contains("body: abcd"));
    }

}
