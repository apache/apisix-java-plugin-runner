package org.apache.apisix.plugin.runner.exception;

import io.netty.channel.ChannelHandlerContext;

public interface ExceptionCaught {

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception;

}
