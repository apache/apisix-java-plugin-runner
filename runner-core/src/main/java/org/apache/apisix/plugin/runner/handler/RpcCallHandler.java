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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.A6Conf;
import org.apache.apisix.plugin.runner.A6ErrRequest;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.ExtraInfoRequest;
import org.apache.apisix.plugin.runner.ExtraInfoResponse;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.HttpResponse;
import org.apache.apisix.plugin.runner.PostRequest;
import org.apache.apisix.plugin.runner.PostResponse;
import org.apache.apisix.plugin.runner.exception.ApisixException;
import org.apache.apisix.plugin.runner.constants.Constants;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
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
public class RpcCallHandler extends SimpleChannelInboundHandler<A6Request> {

    private final Logger logger = LoggerFactory.getLogger(RpcCallHandler.class);

    private final static String EXTRA_INFO_REQ_BODY_KEY = "request_body";
    private final static String EXTRA_INFO_RESP_BODY_KEY = "response_body";

    private final Cache<Long, A6Conf> cache;

    /**
     * the name of the nginx variable to be queried with queue staging
     * whether thread-safe collections are required?
     */
    private final Queue<String> queue = new LinkedList<>();

    private HttpRequest currReq;

    private PostRequest postReq;

    private HttpResponse currResp;

    private PostResponse postResp;

    private long confToken;

    Map<String, String> nginxVars = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, A6Request request) {
        try {
            if (request instanceof A6ErrRequest) {
                errorHandle(ctx, ((A6ErrRequest) request).getCode());
                return;
            }

            if (request.getType() == Constants.RPC_EXTRA_INFO) {
                assert request instanceof ExtraInfoResponse;
                handleExtraInfo(ctx, (ExtraInfoResponse) request);
            }

            if (request.getType() == Constants.RPC_HTTP_REQ_CALL) {
                assert request instanceof HttpRequest;
                handleHttpReqCall(ctx, (HttpRequest) request);
            }

            if (request.getType() == Constants.RPC_HTTP_RESP_CALL) {
                assert request instanceof PostRequest;
                handleHttpRespCall(ctx, (PostRequest) request);
            }
        } catch (Exception e) {
            logger.error("handle request error: ", e);
            errorHandle(ctx, Code.SERVICE_UNAVAILABLE);
        }
    }

    private Boolean[] fetchExtraInfo(ChannelHandlerContext ctx, PluginFilterChain chain) {
        // fetch the nginx variables
        Set<String> varKeys = new HashSet<>();
        boolean requiredReqBody = false;
        boolean requiredVars = false;
        boolean requiredRespBody = false;

        for (PluginFilter filter : chain.getFilters()) {
            Collection<String> vars = filter.requiredVars();
            if (!CollectionUtils.isEmpty(vars)) {
                varKeys.addAll(vars);
                requiredVars = true;
            }

            if (filter.requiredBody() != null && filter.requiredBody()) {
                requiredReqBody = true;
            }

            if (filter.requiredRespBody() != null && filter.requiredRespBody()) {
                requiredRespBody = true;
            }
        }

        // fetch the nginx vars
        if (requiredVars) {
            for (String varKey : varKeys) {
                boolean offer = queue.offer(varKey);
                if (!offer) {
                    logger.error("queue is full");
                    errorHandle(ctx, Code.SERVICE_UNAVAILABLE);
                }
                ExtraInfoRequest extraInfoRequest = new ExtraInfoRequest(varKey, null, null);
                ChannelFuture future = ctx.writeAndFlush(extraInfoRequest);
                future.addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        }

        // fetch the request body
        if (requiredReqBody) {
            queue.offer(EXTRA_INFO_REQ_BODY_KEY);
            ExtraInfoRequest extraInfoRequest = new ExtraInfoRequest(null, true, null);
            ChannelFuture future = ctx.writeAndFlush(extraInfoRequest);
            future.addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }

        // fetch the response body
        if (requiredRespBody) {
            queue.offer(EXTRA_INFO_RESP_BODY_KEY);
            ExtraInfoRequest extraInfoRequest = new ExtraInfoRequest(null, null, true);
            ChannelFuture future = ctx.writeAndFlush(extraInfoRequest);
            future.addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }

        return new Boolean[]{requiredVars, requiredReqBody, requiredRespBody};
    }

    private void handleHttpRespCall(ChannelHandlerContext ctx, PostRequest request) {
        cleanCtx();

        // save HttpCallRequest
        postReq = request;
        postResp = new PostResponse(postReq.getRequestId());

        confToken = postReq.getConfToken();
        A6Conf conf = cache.getIfPresent(confToken);
        if (Objects.isNull(conf)) {
            logger.warn("cannot find conf token: {}", confToken);
            errorHandle(ctx, Code.CONF_TOKEN_NOT_FOUND);
            return;
        }

        PluginFilterChain chain = conf.getChain();

        if (Objects.isNull(chain) || 0 == chain.getFilters().size()) {
            ChannelFuture future = ctx.writeAndFlush(postResp);
            future.addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            return;
        }

        Boolean[] result = fetchExtraInfo(ctx, chain);
        if (Objects.isNull(result)) {
            return;
        }
        if (!result[0] && !result[2]) {
            // no need to fetch extra info
            doPostFilter(ctx);
        }
    }

    private void doPostFilter(ChannelHandlerContext ctx) {
        A6Conf conf = cache.getIfPresent(confToken);
        if (Objects.isNull(conf)) {
            logger.warn("cannot find conf token: {}", confToken);
            errorHandle(ctx, Code.CONF_TOKEN_NOT_FOUND);
            return;
        }

        postReq.initCtx(conf.getConfig());
        postReq.setVars(nginxVars);

        PluginFilterChain chain = conf.getChain()
            .addFilter(new PluginFilter() {
                @Override
                public String name() {
                    return null;
                }

                @Override
                public void postFilter(PostRequest request, PostResponse response, PluginFilterChain chain) {
                    ChannelFuture future = ctx.writeAndFlush(postResp);
                    future.addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                }
            });
        chain.postFilter(postReq, postResp);
    }

    private void handleExtraInfo(ChannelHandlerContext ctx, ExtraInfoResponse request) {
        byte[] result = request.getResult();
        String varsKey = queue.poll();
        if (Objects.isNull(varsKey)) {
            logger.error("queue is empty");
            errorHandle(ctx, Code.SERVICE_UNAVAILABLE);
            return;
        }

        if (EXTRA_INFO_REQ_BODY_KEY.equals(varsKey)) {
            if (!Objects.isNull(currReq)) {
                currReq.setBody(result);
            }
        } else if (EXTRA_INFO_RESP_BODY_KEY.equals(varsKey)) {
            if (!Objects.isNull(postReq)) {
                postReq.setBody(result);
            }
        }
        else {
            nginxVars.put(varsKey, new String(result));
        }

        if (queue.isEmpty()) {
            if (currReq != null) {
                doFilter(ctx);
            } else if (postReq != null) {
                doPostFilter(ctx);
            }
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

        PluginFilterChain chain = conf.getChain()
                .addFilter(new PluginFilter() {
                    @Override
                    public String name() {
                        return "writeFilter";
                    }

                    @Override
                    public void filter(HttpRequest request, HttpResponse response, PluginFilterChain chain) {
                        ChannelFuture future = ctx.writeAndFlush(currResp);
                        future.addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                });

        chain.filter(currReq, currResp);
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

        // here we pre-read parameters in the req to
        // prevent confusion over the read/write index of the req.
        preReadReq();

        // if the filter chain is empty, then return the response directly
        if (Objects.isNull(chain) || 0 == chain.getFilters().size()) {
            ChannelFuture future = ctx.writeAndFlush(currResp);
            future.addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            return;
        }

        Boolean[] result = fetchExtraInfo(ctx, chain);
        if (Objects.isNull(result)) {
            return;
        }
        if (!result[0] && !result[1]) {
            // no need to fetch extra info
            doFilter(ctx);
        }
    }

    private void preReadReq() {
        currReq.getHeaders();
        currReq.getPath();
        currReq.getMethod();
        currReq.getArgs();
        currReq.getSourceIP();
    }

    private void errorHandle(ChannelHandlerContext ctx, int code) {
        throw new ApisixException(code, Code.name(code));
    }

    private void cleanCtx() {
        queue.clear();
        nginxVars.clear();
        currReq = null;
        currResp = null;
        confToken = -1;
    }
}
