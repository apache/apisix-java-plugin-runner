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

package org.apache.apisix.plugin.runner.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.apisix.plugin.runner.A6Conf;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfiguration {

    @Bean
    public Cache<Long, A6Conf> configurationCache(@Value("${cache.config.expired:3610}") long expired,
                                                  @Value("${cache.config.capacity:1000}") int capacity) {
        return CacheBuilder.newBuilder().expireAfterWrite(expired, TimeUnit.SECONDS).maximumSize(capacity).build();
    }
}
