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
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.A6ErrResponse;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.A6Response;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@RequiredArgsConstructor
public class A6HttpCallHandler implements Handler {
    private final Logger logger = LoggerFactory.getLogger(A6HttpCallHandler.class);

    private final Cache<Long, A6Conf> cache;

    @Override
    public void handle(A6Request request, A6Response response) {
        HttpRequest req = (HttpRequest) request;
        HttpResponse rsp = (HttpResponse) response;

        long confToken = ((HttpRequest) request).getConfToken();
        A6Conf conf = cache.getIfPresent(confToken);
        if (Objects.isNull(conf)) {
            logger.warn("cannot find conf token: {}", confToken);
            A6ErrResponse errResponse = new A6ErrResponse(Code.CONF_TOKEN_NOT_FOUND);
            rsp.setErrResponse(errResponse);
            return;
        }

        req.initCtx(rsp, conf.getReq());
        PluginFilterChain chain = conf.getChain();
        chain.filter(req, rsp);
    }

}
