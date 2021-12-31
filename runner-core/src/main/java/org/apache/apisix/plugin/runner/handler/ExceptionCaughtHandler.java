package org.apache.apisix.plugin.runner.handler;

import io.github.api7.A6.Err.Code;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.apisix.plugin.runner.A6ErrResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionCaughtHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(ExceptionCaughtHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("handle request error: ", cause);
        A6ErrResponse errResponse = new A6ErrResponse(Code.SERVICE_UNAVAILABLE);
        ctx.writeAndFlush(errResponse);
    }
}
