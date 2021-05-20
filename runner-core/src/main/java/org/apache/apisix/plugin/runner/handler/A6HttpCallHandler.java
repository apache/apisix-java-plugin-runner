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
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.A6Config;
import org.apache.apisix.plugin.runner.A6ErrResponse;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.A6Response;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.filter.FilterChain;

@RequiredArgsConstructor
public class A6HttpCallHandler implements Handler {
    private final Cache<Long, Req> cache;

    private final FilterChain chain;

    @Override
    public void handle(A6Request request, A6Response response) {
        HttpRequest req = (HttpRequest) request;
        HttpResponse rsp = (HttpResponse) response;

        long confToken = ((HttpRequest) request).getConfToken();
        io.github.api7.A6.PrepareConf.Req conf = cache.getIfPresent(confToken);
        if (null == conf) {
            A6ErrResponse errResponse = new A6ErrResponse(Code.CONF_TOKEN_NOT_FOUND);
            rsp.setErrResponse(errResponse);
            return;
        }

        A6Config config = new A6Config(conf);
        req.setConfig(config);
        chain.doFilter(req, rsp);

    }
}
