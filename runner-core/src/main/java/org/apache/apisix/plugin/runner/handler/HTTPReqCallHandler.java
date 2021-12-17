package org.apache.apisix.plugin.runner.handler;

import com.google.common.cache.Cache;
import io.github.api7.A6.Err.Code;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.apache.apisix.plugin.runner.*;
import org.apache.apisix.plugin.runner.filter.PluginFilter;
import org.apache.apisix.plugin.runner.filter.PluginFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class HTTPReqCallHandler extends SimpleChannelInboundHandler<A6Request> {

    private final Logger logger = LoggerFactory.getLogger(HTTPReqCallHandler.class);

    private final static String extraInfoReqBodyKey = "request_body";

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
            A6ErrResponse errResponse = new A6ErrResponse(Code.SERVICE_UNAVAILABLE);
            ctx.writeAndFlush(errResponse);
        }

    }

    private void handleExtraInfo(ChannelHandlerContext ctx, ExtraInfoResponse request) {
        String result = request.getResult();
        String varsKey = queue.remove();


        if (varsKey.equals(extraInfoReqBodyKey)) {
            currReq.setBody(result);
        }else {
            nginxVars.put(varsKey, result);
        }
        if (queue.isEmpty()) {
            A6Conf conf = cache.getIfPresent(confToken);
            if (Objects.isNull(conf)) {
                logger.warn("cannot find conf token: {}", confToken);
                A6ErrResponse errResponse = new A6ErrResponse(Code.CONF_TOKEN_NOT_FOUND);
                ctx.writeAndFlush(errResponse);
                return;
            }

            currReq.initCtx(currResp, conf.getConfig());
            currReq.setVars(nginxVars);

            PluginFilterChain chain = conf.getChain();
            chain.filter(currReq, currResp);

            ctx.writeAndFlush(currResp);
        }
    }


    private void handleHttpReqCall(ChannelHandlerContext ctx, HttpRequest request) {
        cleanCtx();

        currReq = request;
        currResp = new HttpResponse(currReq.getRequestId());

        confToken = currReq.getConfToken();
        A6Conf conf = cache.getIfPresent(confToken);
        if (Objects.isNull(conf)) {
            logger.warn("cannot find conf token: {}", confToken);
            A6ErrResponse errResponse = new A6ErrResponse(Code.CONF_TOKEN_NOT_FOUND);
            ctx.writeAndFlush(errResponse);
            return;
        }

        PluginFilterChain chain = conf.getChain();

        //TODO if the filter chain is empty, then return the response directly

        // fetch the nginx variables
        Set<String> varKeys = new HashSet<>();
        boolean requiredBody = false;

        for (PluginFilter filter : chain.getFilters()) {
            Collection<String> vars = filter.requiredVars();
            if (!CollectionUtils.isEmpty(vars)) {
                varKeys.addAll(vars);
            }

            if (filter.requiredBody()) {
                requiredBody = true;
            }
        }

        for (String varKey : varKeys) {
            queue.add(varKey);
            ExtraInfoRequest extraInfoRequest = new ExtraInfoRequest(varKey, null);
            ctx.writeAndFlush(extraInfoRequest);
        }

        //fetch request body
        fetchBody(ctx, requiredBody);
    }

    private void fetchBody(ChannelHandlerContext ctx, boolean requiredBody) {
        if (requiredBody) {
            queue.add(extraInfoReqBodyKey);
            ExtraInfoRequest extraInfoRequest = new ExtraInfoRequest(null, true);
            ctx.writeAndFlush(extraInfoRequest);
        }
    }

    private void cleanCtx() {
        queue.clear();
        nginxVars.clear();
        currReq = null;
        currResp = null;
        confToken = -1;
    }
}