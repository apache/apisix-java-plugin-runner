package org.apache.apisix.plugin.runner.handler;

import io.github.api7.A6.Err.Code;
import io.netty.channel.ChannelHandlerContext;
import org.apache.apisix.plugin.runner.PostResponse;
import org.apache.apisix.plugin.runner.exception.ExceptionCaught;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalException implements ExceptionCaught {

    private final Logger logger = LoggerFactory.getLogger(GlobalException.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // expand exception
        logger.warn("plugin expend");
        PostResponse errResponse = new PostResponse(Code.SERVICE_UNAVAILABLE);
        errResponse.setStatusCode(10001);
        errResponse.setBody("test exception");
        ctx.writeAndFlush(errResponse);
    }
}
