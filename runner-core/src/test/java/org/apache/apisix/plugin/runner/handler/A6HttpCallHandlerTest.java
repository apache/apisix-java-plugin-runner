///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.apisix.plugin.runner.handler;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.google.flatbuffers.FlatBufferBuilder;
//import com.google.gson.Gson;
//import io.github.api7.A6.Err.Code;
//import io.github.api7.A6.HTTPReqCall.Action;
//import io.github.api7.A6.TextEntry;
//import org.apache.apisix.plugin.runner.A6Conf;
//import org.apache.apisix.plugin.runner.A6ConfigRequest;
//import org.apache.apisix.plugin.runner.A6ConfigResponse;
//import org.apache.apisix.plugin.runner.HttpRequest;
//import org.apache.apisix.plugin.runner.HttpResponse;
//import org.apache.apisix.plugin.runner.filter.PluginFilter;
//import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import reactor.core.publisher.Mono;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.TimeUnit;
//
//@DisplayName("test filter handle")
//class A6HttpCallHandlerTest {
//    private PrintStream console = null;
//    private ByteArrayOutputStream bytes = null;
//
//    A6HttpCallHandler a6HttpCallHandler;
//
//    Cache<Long, A6Conf> cache;
//
//    Map<String, PluginFilter> filters;
//
//    @BeforeEach
//    void setUp() {
//
//        bytes = new ByteArrayOutputStream();
//        console = System.out;
//        System.setOut(new PrintStream(bytes));
//
//        filters = new HashMap<>();
//        filters.put("FooFilter", new PluginFilter() {
//            @Override
//            public String name() {
//                return "FooFilter";
//            }
//
//            @Override
//            @SuppressWarnings("unchecked")
//            public Mono<Void> filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
//                System.out.println("do filter: FooFilter, order: " + chain.getIndex());
//                System.out.println("do filter: FooFilter, config: " + request.getConfig(this));
//                Gson gson = new Gson();
//                Map<String, Object> conf = new HashMap<>();
//                conf = gson.fromJson(request.getConfig(this), conf.getClass());
//                System.out.println("do filter: FooFilter, conf_key1 value: " + conf.get("conf_key1"));
//                System.out.println("do filter: FooFilter, conf_key2 value: " + conf.get("conf_key2"));
//                if (!Objects.isNull(request.getPath())) {
//                    System.out.println("do filter: path: " + request.getPath());
//                }
//
//                if (!Objects.isNull(request.getArgs())) {
//                    for (Map.Entry<String, String> arg : request.getArgs().entrySet()) {
//                        System.out.println("do filter: arg key: " + arg.getKey());
//                        System.out.println("do filter: arg value: " + arg.getValue());
//                    }
//                }
//
//                if (!Objects.isNull(request.getHeader())) {
//                    for (Map.Entry<String, String> header : request.getHeader().entrySet()) {
//                        System.out.println("do filter: header key: " + header.getKey());
//                        System.out.println("do filter: header value: " + header.getValue());
//                    }
//                }
//
//                if (!Objects.isNull(request.getMethod())) {
//                    System.out.println("do filter: method: " + request.getMethod());
//                }
//
//                return chain.filter(request, response);
//            }
//
//            @Override
//            public Collection<String> fetchVarsInAdvance() {
//                return null;
//            }
//
//        });
//
//        filters.put("CatFilter", new PluginFilter() {
//            @Override
//            public String name() {
//                return "CatFilter";
//            }
//
//            @Override
//            public Mono<Void> filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
//                System.out.println("do filter: CatFilter, order: " + chain.getIndex());
//                System.out.println("do filter: CatFilter, config: " + request.getConfig(this));
//
//                response.setStatusCode(401);
//                return chain.filter(request, response);
//            }
//
//            @Override
//            public Collection<String> fetchVarsInAdvance() {
//                return null;
//            }
//
//        });
//        cache = CacheBuilder.newBuilder().expireAfterWrite(3600, TimeUnit.SECONDS).maximumSize(1000).build();
//        FlatBufferBuilder builder = new FlatBufferBuilder();
//
//        int foo = builder.createString("FooFilter");
//        int bar = builder.createString("{\"conf_key1\":\"conf_value1\",\"conf_key2\":2}");
//        int filter1 = TextEntry.createTextEntry(builder, foo, bar);
//
//        int cat = builder.createString("CatFilter");
//        int dog = builder.createString("Dog");
//        int filter2 = TextEntry.createTextEntry(builder, cat, dog);
//
//        int confVector = io.github.api7.A6.PrepareConf.Req.createConfVector(builder, new int[]{filter1, filter2});
//        io.github.api7.A6.PrepareConf.Req.startReq(builder);
//        io.github.api7.A6.PrepareConf.Req.addConf(builder, confVector);
//        builder.finish(io.github.api7.A6.PrepareConf.Req.endReq(builder));
//        io.github.api7.A6.PrepareConf.Req req = io.github.api7.A6.PrepareConf.Req.getRootAsReq(builder.dataBuffer());
//
//        A6ConfigRequest request = new A6ConfigRequest(req);
//        A6ConfigResponse response = new A6ConfigResponse(0L);
//
//        A6ConfigHandler a6ConfigHandler = new A6ConfigHandler(cache, filters);
//        a6ConfigHandler.handle(request, response);
//
//        a6HttpCallHandler = new A6HttpCallHandler(cache);
//    }
//
//    @AfterEach
//    void setDown() {
//        System.setOut(console);
//    }
//
//    @Test
//    @DisplayName("test cannot find conf token")
//    void testCannotFindConfToken() {
//        FlatBufferBuilder builder = new FlatBufferBuilder();
//
//        io.github.api7.A6.HTTPReqCall.Req.startReq(builder);
//        io.github.api7.A6.HTTPReqCall.Req.addConfToken(builder, 9999L);
//        builder.finish(io.github.api7.A6.HTTPReqCall.Req.endReq(builder));
//
//        io.github.api7.A6.HTTPReqCall.Req req = io.github.api7.A6.HTTPReqCall.Req.getRootAsReq(builder.dataBuffer());
//        HttpRequest request = new HttpRequest(req);
//        HttpResponse response = new HttpResponse(1L);
//        a6HttpCallHandler.handle(request, response);
//        io.github.api7.A6.Err.Resp err = io.github.api7.A6.Err.Resp.getRootAsResp(response.getErrResponse().encode());
//        Assertions.assertEquals(err.code(), Code.CONF_TOKEN_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("test do filter and get config")
//    void testDoFilter1() {
//        FlatBufferBuilder builder = new FlatBufferBuilder();
//
//        io.github.api7.A6.HTTPReqCall.Req.startReq(builder);
//        io.github.api7.A6.HTTPReqCall.Req.addConfToken(builder, 0L);
//        builder.finish(io.github.api7.A6.HTTPReqCall.Req.endReq(builder));
//
//        io.github.api7.A6.HTTPReqCall.Req req = io.github.api7.A6.HTTPReqCall.Req.getRootAsReq(builder.dataBuffer());
//        HttpRequest request = new HttpRequest(req);
//        HttpResponse response = new HttpResponse(1L);
//        a6HttpCallHandler.handle(request, response);
//        Assertions.assertTrue(bytes.toString().contains("do filter: FooFilter, order: 1"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: FooFilter, order: 1"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: FooFilter, config: {\"conf_key1\":\"conf_value1\",\"conf_key2\":2}"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: FooFilter, conf_key1 value: conf_value1"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: FooFilter, conf_key2 value: 2.0"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: CatFilter, order: 2"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: CatFilter, config: Dog"));
//    }
//
//    @Test
//    @DisplayName("test get request params")
//    void testDoFilter2() {
//        FlatBufferBuilder builder = new FlatBufferBuilder();
//        int argKey = builder.createString("argKey");
//        int argValue = builder.createString("argValue");
//        int arg = TextEntry.createTextEntry(builder, argKey, argValue);
//        int argsVector = io.github.api7.A6.HTTPReqCall.Req.createArgsVector(builder, new int[]{arg});
//
//        int headerKey = builder.createString("headerKey");
//        int headerValue = builder.createString("headerValue");
//
//        int header = TextEntry.createTextEntry(builder, headerKey, headerValue);
//        int headerVector =
//                io.github.api7.A6.HTTPReqCall.Req.createHeadersVector(builder, new int[]{header});
//
//        int path = builder.createString("/path");
//
//        io.github.api7.A6.HTTPReqCall.Req.startReq(builder);
//        io.github.api7.A6.HTTPReqCall.Req.addId(builder, 8888L);
//        io.github.api7.A6.HTTPReqCall.Req.addConfToken(builder, 0L);
//        io.github.api7.A6.HTTPReqCall.Req.addMethod(builder, io.github.api7.A6.Method.GET);
//        io.github.api7.A6.HTTPReqCall.Req.addHeaders(builder, headerVector);
//        io.github.api7.A6.HTTPReqCall.Req.addPath(builder, path);
//        io.github.api7.A6.HTTPReqCall.Req.addArgs(builder, argsVector);
//        builder.finish(io.github.api7.A6.HTTPReqCall.Req.endReq(builder));
//        io.github.api7.A6.HTTPReqCall.Req req = io.github.api7.A6.HTTPReqCall.Req.getRootAsReq(builder.dataBuffer());
//
//        HttpRequest request = new HttpRequest(req);
//        HttpResponse response = new HttpResponse(1L);
//        a6HttpCallHandler.handle(request, response);
//        Assertions.assertTrue(bytes.toString().contains("do filter: FooFilter, order: 1"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: FooFilter, config: {\"conf_key1\":\"conf_value1\",\"conf_key2\":2}"));
//
//        Assertions.assertTrue(bytes.toString().contains("do filter: path: /path"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: arg key: argKey"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: arg value: argValue"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: header key: headerKey"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: header value: headerValue"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: method: GET"));
//
//        Assertions.assertTrue(bytes.toString().contains("do filter: CatFilter, order: 2"));
//        Assertions.assertTrue(bytes.toString().contains("do filter: CatFilter, config: Dog"));
//    }
//
//    @Test
//    @DisplayName("test stop the request")
//    void testDoFilter3() {
//        FlatBufferBuilder builder = new FlatBufferBuilder();
//
//        io.github.api7.A6.HTTPReqCall.Req.startReq(builder);
//        io.github.api7.A6.HTTPReqCall.Req.addConfToken(builder, 0L);
//        builder.finish(io.github.api7.A6.HTTPReqCall.Req.endReq(builder));
//
//        io.github.api7.A6.HTTPReqCall.Req req = io.github.api7.A6.HTTPReqCall.Req.getRootAsReq(builder.dataBuffer());
//        HttpRequest request = new HttpRequest(req);
//        HttpResponse response = new HttpResponse(1L);
//        a6HttpCallHandler.handle(request, response);
//
//        io.github.api7.A6.HTTPReqCall.Resp resp =
//                io.github.api7.A6.HTTPReqCall.Resp.getRootAsResp(response.encode());
//        Assertions.assertEquals(resp.actionType(), Action.Stop);
//    }
//}
