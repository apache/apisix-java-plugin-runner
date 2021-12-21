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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.ExtraInfoResponse;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
import org.apache.apisix.plugin.runner.A6ErrResponse;
import org.apache.apisix.plugin.runner.ExtraInfoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

@RequiredArgsConstructor
public class HTTPReqCallHandler extends SimpleChannelInboundHandler<A6Request> {

    private final Logger logger = LoggerFactory.getLogger(HTTPReqCallHandler.class);

    private final static String EXTRA_INFO_REQ_BODY_KEY = "request_body";

    private final Cache<Long, A6Conf> cache;

    /**
     * the name of the nginx variable to be queried with queue staging
     * whether thread-safe collections are required?
     */
    private Queue<String> queue = new LinkedList<>();

    private HttpRequest currReq;

    private HttpResponse currResp;

    private long confToken;

    Map<String, String> nginxVars = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, A6Request request) {
        try {
            if (request.getType() == 3) {
                handleExtraInfo(ctx, (ExtraInfoResponse) request);
            }

            if (request.getType() == 2) {
                handleHttpReqCall(ctx, (HttpRequest) request);
            }
        } catch (Exception e) {
            logger.error("handle request error: ", e);
            errorHandle(ctx, Code.SERVICE_UNAVAILABLE);
        }

    }

    private void handleExtraInfo(ChannelHandlerContext ctx, ExtraInfoResponse request) {
        String result = request.getResult();
        String varsKey = queue.poll();
        if (Objects.isNull(varsKey)) {
            logger.error("queue is empty");
            errorHandle(ctx, Code.SERVICE_UNAVAILABLE);
            return;
        }

        if (varsKey.equals(EXTRA_INFO_REQ_BODY_KEY)) {
            currReq.setBody(result);
        } else {
            nginxVars.put(varsKey, result);
        }
        if (queue.isEmpty()) {
            doFilter(ctx);
        }
    }

    private void doFilter(ChannelHandlerContext ctx) {
        A6Conf conf = cache.getIfPresent(confToken);
        if (Objects.isNull(conf)) {
            logger.warn("cannot find conf token: {}", confToken);
            errorHandle(ctx, Code.CONF_TOKEN_NOT_FOUND);
            return;
        }

        currReq.initCtx(currResp, conf.getConfig());
        currReq.setVars(nginxVars);

        PluginFilterChain chain = conf.getChain();
        chain.filter(currReq, currResp);

        ctx.writeAndFlush(currResp);
    }

    private void handleHttpReqCall(ChannelHandlerContext ctx, HttpRequest request) {
        cleanCtx();

        // save HttpCallRequest
        currReq = request;
        currResp = new HttpResponse(currReq.getRequestId());

        confToken = currReq.getConfToken();
        A6Conf conf = cache.getIfPresent(confToken);
        if (Objects.isNull(conf)) {
            logger.warn("cannot find conf token: {}", confToken);
            errorHandle(ctx, Code.CONF_TOKEN_NOT_FOUND);
            return;
        }

        PluginFilterChain chain = conf.getChain();

        // if the filter chain is empty, then return the response directly
        if (Objects.isNull(chain) || 0 == chain.getFilters().size()) {
            ctx.writeAndFlush(currResp);
            return;
        }

        // fetch the nginx variables
        Set<String> varKeys = new HashSet<>();
        boolean requiredBody = false;
        boolean requiredVars = false;

        for (PluginFilter filter : chain.getFilters()) {
            Collection<String> vars = filter.requiredVars();
            if (!CollectionUtils.isEmpty(vars)) {
                varKeys.addAll(vars);
                requiredVars = true;
            }

            if (filter.requiredBody() != null && filter.requiredBody()) {
                requiredBody = true;
            }
        }

        if (varKeys.size() > 0) {
            for (String varKey : varKeys) {
                boolean offer = queue.offer(varKey);
                if (!offer) {
                    logger.error("queue is full");
                    errorHandle(ctx, Code.SERVICE_UNAVAILABLE);
                    return;
                }
                ExtraInfoRequest extraInfoRequest = new ExtraInfoRequest(varKey, null);
                ctx.writeAndFlush(extraInfoRequest);
            }
        }

        // fetch the request body
        if (requiredBody) {
            queue.offer(EXTRA_INFO_REQ_BODY_KEY);
            ExtraInfoRequest extraInfoRequest = new ExtraInfoRequest(null, true);
            ctx.writeAndFlush(extraInfoRequest);
        }

        // no need to fetch the nginx variables or request body, just do filter
        if (!requiredBody && !requiredVars) {
            doFilter(ctx);
        }
    }

    private void errorHandle(ChannelHandlerContext ctx, int code) {
        A6ErrResponse errResponse = new A6ErrResponse(code);
        ctx.writeAndFlush(errResponse);
    }

    private void cleanCtx() {
        queue.clear();
        nginxVars.clear();
        currReq = null;
        currResp = null;
        confToken = -1;
    }
}
