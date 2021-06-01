package org.apache.apisix.plugin.runner.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class DelayedDecoder extends LengthFieldBasedFrameDecoder {

    public DelayedDecoder() {
        super(16777215, 0, 0);
    }

    @Override
    protected ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) {
        in.readByte();
        int length = in.readMedium();
        if (in.readableBytes() < length) {
            return null;
        }
        in.readerIndex(0);

        int readLength = in.readableBytes();
        return in.retainedSlice(0, readLength);
    }
}