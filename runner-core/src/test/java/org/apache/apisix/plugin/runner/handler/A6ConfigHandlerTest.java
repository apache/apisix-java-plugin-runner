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
import io.github.api7.A6.PrepareConf.Req;
import io.github.api7.A6.TextEntry;
import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6ConfigResponse;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ExtendWith(OutputCaptureExtension.class)
@DisplayName("test add filter")
class A6ConfigHandlerTest {

    Cache<Long, A6Conf> cache;

    Map<String, PluginFilter> filters;

    A6ConfigHandler a6ConfigHandler;

    @BeforeEach
    void setUp() {
        filters = new HashMap<>();
        filters.put("FooFilter", new PluginFilter() {
            @Override
            public String name() {
                return "FooFilter";
            }

            @Override
            public Mono<Void> filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
                return chain.filter(request, response);
            }
        });

        filters.put("CatFilter", new PluginFilter() {
            @Override
            public String name() {
                return "CatFilter";
            }

            @Override
            public Mono<Void> filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
                return chain.filter(request, response);
            }
        });
        cache = CacheBuilder.newBuilder().expireAfterWrite(3600, TimeUnit.SECONDS).maximumSize(1000).build();
        a6ConfigHandler = new A6ConfigHandler(cache, filters);
    }

    @Test
    @DisplayName("test add filter by prepare conf")
    void testAddFilter1() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int name = builder.createString("FooFilter");
        int value = builder.createString("Bar");
        int conf = TextEntry.createTextEntry(builder, name, value);
        int confVector = Req.createConfVector(builder, new int[]{conf});
        Req.startReq(builder);
        Req.addConf(builder, confVector);
        builder.finish(Req.endReq(builder));
        Req req = Req.getRootAsReq(builder.dataBuffer());

        A6ConfigRequest request = new A6ConfigRequest(req);
        A6ConfigResponse response = new A6ConfigResponse(0L);
        a6ConfigHandler.handle(request, response);

        A6Conf config = cache.getIfPresent(0L);
        Assertions.assertNotNull(config.getChain());
        Assertions.assertEquals(config.getChain().getFilters().size(), 1);
        Assertions.assertEquals(config.getChain().getIndex(), 0);
        Assertions.assertEquals(config.get("FooFilter"), "Bar");

    }

    @Test
    @DisplayName("test filter sort by it's order")
    void testAddFilter2() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int cat = builder.createString("CatFilter");
        int dog = builder.createString("Dog");
        int filter2 = TextEntry.createTextEntry(builder, cat, dog);

        int foo = builder.createString("FooFilter");
        int bar = builder.createString("Bar");
        int filter1 = TextEntry.createTextEntry(builder, foo, bar);

        int confVector = Req.createConfVector(builder, new int[]{filter1, filter2});
        Req.startReq(builder);
        Req.addConf(builder, confVector);
        builder.finish(Req.endReq(builder));
        Req req = Req.getRootAsReq(builder.dataBuffer());

        A6ConfigRequest request = new A6ConfigRequest(req);
        A6ConfigResponse response = new A6ConfigResponse(0L);
        a6ConfigHandler.handle(request, response);

        A6Conf config = cache.getIfPresent(0L);
        Assertions.assertEquals(config.getChain().getFilters().size(), 2);
    }

    @Test
    @Disabled
    @DisplayName("test skip the same name filter")
    void testAddFilter3(CapturedOutput capturedOutput) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int foo1 = builder.createString("FooFilter");
        int bar1 = builder.createString("Bar1");
        int filter1 = TextEntry.createTextEntry(builder, foo1, bar1);

        int foo2 = builder.createString("FooFilter");
        int bar2 = builder.createString("Bar2");
        int filter2 = TextEntry.createTextEntry(builder, foo2, bar2);

        int confVector = Req.createConfVector(builder, new int[]{filter1, filter2});
        Req.startReq(builder);
        Req.addConf(builder, confVector);
        builder.finish(Req.endReq(builder));
        Req req = Req.getRootAsReq(builder.dataBuffer());

        A6ConfigRequest request = new A6ConfigRequest(req);
        A6ConfigResponse response = new A6ConfigResponse(0L);
        a6ConfigHandler.handle(request, response);

        A6Conf config = cache.getIfPresent(0L);
        Assertions.assertEquals(config.getChain().getFilters().size(), 1);
        Assertions.assertTrue(capturedOutput.getOut().contains("skip the same filter: FooFilter"));
    }

    @Test
    @Disabled
    @DisplayName("test receive undefined filter")
    void testAddFilter4(CapturedOutput capturedOutput) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int foo = builder.createString("UndefinedFilter");
        int bar = builder.createString("Bar");
        int filter = TextEntry.createTextEntry(builder, foo, bar);

        int confVector = Req.createConfVector(builder, new int[]{filter});
        Req.startReq(builder);
        Req.addConf(builder, confVector);
        builder.finish(Req.endReq(builder));
        Req req = Req.getRootAsReq(builder.dataBuffer());

        A6ConfigRequest request = new A6ConfigRequest(req);
        A6ConfigResponse response = new A6ConfigResponse(0L);
        a6ConfigHandler.handle(request, response);

        A6Conf config = cache.getIfPresent(0L);
        Assertions.assertEquals(config.getChain().getFilters().size(), 0);
        Assertions.assertTrue(capturedOutput.getOut().contains("receive undefined filter: UndefinedFilter, skip it"));
    }
}
