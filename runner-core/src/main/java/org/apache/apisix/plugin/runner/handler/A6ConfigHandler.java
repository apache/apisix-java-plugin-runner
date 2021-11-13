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
import io.github.api7.A6.PrepareConf.Req;
import io.github.api7.A6.TextEntry;
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6ConfigResponse;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.A6Response;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handle APISIX configuration request.
 */
@RequiredArgsConstructor
public class A6ConfigHandler implements Handler {
    private final Logger logger = LoggerFactory.getLogger(A6ConfigHandler.class);

    private final Cache<Long, A6Conf> cache;
    private final Map<String, PluginFilter> filters;

    @Override
    public void handle(A6Request request, A6Response response) {
        Req req = ((A6ConfigRequest) request).getReq();
        long token = ((A6ConfigResponse) response).getConfToken();
        PluginFilterChain chain = createFilterChain(req);

        /*
         * to reset vtable_start and vtable_size of req,
         * so that req can be reused after being got from the cache.
         * {@link org.apache.apisix.plugin.runner.handler.A6HttpCallHandler#handle cache.getIfPresent()}
         * @see <a href="Issues63"> https://github.com/apache/apisix-java-plugin-runner/issues/63</a>
         * */
        Map<String, String> config = new HashMap<>();
        for (int i = 0; i < req.confLength(); i++) {
            TextEntry conf = req.conf(i);
            config.put(conf.name(), conf.value());
        }
        A6Conf a6Conf = new A6Conf(config, chain);
        cache.put(token, a6Conf);
    }

    private PluginFilterChain createFilterChain(Req req) {
        List<PluginFilter> chainFilters = new ArrayList<>();
        for (int i = 0; i < req.confLength(); i++) {
            TextEntry conf = req.conf(i);
            PluginFilter filter = filters.get(conf.name());
            if (Objects.isNull(filter)) {
                logger.warn("receive undefined filter: {}, skip it", conf.name());
                continue;
            }
            if (chainFilters.contains(filter)) {
                logger.warn("skip the same filter: {}", conf.name());
                continue;
            }
            chainFilters.add(filter);
        }
        return new PluginFilterChain(chainFilters);
    }

}
