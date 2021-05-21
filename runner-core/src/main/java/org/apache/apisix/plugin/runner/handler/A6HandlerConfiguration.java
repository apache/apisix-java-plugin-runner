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
import io.github.api7.A6.Err.Code;
import io.github.api7.A6.PrepareConf.Req;
import org.apache.apisix.plugin.runner.A6ConfigResponse;
import org.apache.apisix.plugin.runner.A6ErrRequest;
import org.apache.apisix.plugin.runner.A6ErrResponse;
import org.apache.apisix.plugin.runner.A6Response;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.filter.FilterBean;
import org.apache.apisix.plugin.runner.filter.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Configuration
public class A6HandlerConfiguration {
    private final Logger logger = LoggerFactory.getLogger(A6HandlerConfiguration.class);

    @Bean
    public A6ConfigHandler createConfigHandler(Cache<Long, Req> cache) {
        return new A6ConfigHandler(cache);
    }

    @Bean
    public A6HttpCallHandler createHttpHandler(ObjectProvider<FilterBean> beanProvider, Cache<Long, Req> cache) {
        List<FilterBean> filterList = beanProvider.orderedStream().collect(Collectors.toList());
        FilterChain chain = null;
        if (!filterList.isEmpty()) {
            for (int i = filterList.size() - 1; i >= 0; i--) {
                chain = new FilterChain(filterList.get(i), chain);
            }
        }
        return new A6HttpCallHandler(cache, chain);
    }

    @Bean
    public FilterBean testFilter() {
        return new FilterBean() {
            @Override
            public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {

            }

            @Override
            public int getOrder() {
                return 0;
            }
        };
    }

    @Bean
    public Dispatcher createDispatcher(A6ConfigHandler configHandler, A6HttpCallHandler httpCallHandler) {
        return request -> {
            A6Response response;
            switch (request.getType()) {
                case 0:
                    response = new A6ErrResponse(((A6ErrRequest) request).getCode());
                    return response;
                case 1:
                    long confToken = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
                    response = new A6ConfigResponse(confToken);
                    configHandler.handle(request, response);
                    return response;
                case 2:
                    response = new HttpResponse(((HttpRequest) request).getRequestId());
                    httpCallHandler.handle(request, response);
                    return response;
                default:
                    logger.error("can not dispatch type: {}", request.getType());
                    response = new A6ErrResponse(Code.SERVICE_UNAVAILABLE);
                    return response;
            }
        };
    }
}
