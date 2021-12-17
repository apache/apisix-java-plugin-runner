package org.apache.apisix.plugin.runner.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.apache.apisix.plugin.runner.A6Response;

import java.nio.ByteBuffer;

public class PayloadEncoder extends ChannelOutboundHandlerAdapter {

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof A6Response) {
            A6Response response = (A6Response) msg;
            ByteBuffer buffer = encode(response);
            ByteBuf buf = Unpooled.wrappedBuffer(buffer);
            ctx.write(buf, promise);
        }
    }


    public ByteBuffer encode(A6Response response) {
        ByteBuffer buffer = response.encode();
        return setBody(buffer, response.getType());
    }

    private ByteBuffer setBody(ByteBuffer payload, byte type) {
        byte[] data = new byte[payload.remaining()];
        payload.get(data);
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 4);
        buffer.put(type);
        // data length
        byte[] length = int2Bytes(data.length, 3);
        buffer.put(length);
        // data
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    byte[] int2Bytes(int value, int len) {
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte) ((value >> 8 * i) & 0xff);
        }
        return b;
    }
}
