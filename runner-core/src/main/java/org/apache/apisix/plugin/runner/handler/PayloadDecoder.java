package org.apache.apisix.plugin.runner.handler;

import io.github.api7.A6.Err.Code;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.apisix.plugin.runner.A6ConfigRequest;
import org.apache.apisix.plugin.runner.A6ErrRequest;
import org.apache.apisix.plugin.runner.A6Request;
import org.apache.apisix.plugin.runner.HttpRequest;
import org.apache.apisix.plugin.runner.ExtraInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class PayloadDecoder extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LoggerFactory.getLogger(PayloadDecoder.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        ByteBuffer buffer = byteBuf.nioBuffer();
        A6Request request = decode(buffer);
        ctx.fireChannelRead(request);
    }

    public A6Request decode(ByteBuffer buffer) {
        byte type;
        try {
            type = buffer.get();
        } catch (BufferUnderflowException e) {
            logger.warn("receive empty data");
            return new A6ErrRequest(Code.BAD_REQUEST);
        }

        ByteBuffer body;
        switch (type) {
            case 1:
                A6ConfigRequest a6ConfigRequest;
                try {
                    body = getBody(buffer);
                    a6ConfigRequest = A6ConfigRequest.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    logger.warn("receive error data length");
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return a6ConfigRequest;
            case 2:
                HttpRequest httpRequest;
                try {
                    body = getBody(buffer);
                    httpRequest = HttpRequest.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return httpRequest;

            case 3:
                ExtraInfoResponse extraInfoResponse;
                try {
                    body = getBody(buffer);
                    extraInfoResponse = ExtraInfoResponse.from(body);
                } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
                    return new A6ErrRequest(Code.BAD_REQUEST);
                }
                return extraInfoResponse;
            default:
                break;
        }

        logger.warn("receive unsupported type: {}", type);
        return new A6ErrRequest(Code.BAD_REQUEST);
    }

    private int getDataLength(ByteBuffer payload) {
        byte[] bytes = new byte[3];
        for (int i = 0; i < 3; i++) {
            bytes[i] = payload.get();
        }
        return bytes2Int(bytes, 0, 3);
    }

    private ByteBuffer getBody(ByteBuffer payload) throws BufferUnderflowException, IndexOutOfBoundsException {
        int length = getDataLength(payload);
        ByteBuffer buffer = payload.slice();
        byte[] dst = new byte[length];
        buffer.get(dst, 0, length);
        buffer.flip();
        return buffer;
    }

    private int bytes2Int(byte[] b, int start, int len) {
        int sum = 0;
        int end = start + len;
        for (int i = start; i < end; i++) {
            int n = b[i] & 0xff;
            n <<= (--len) * 8;
            sum += n;
        }
        return sum;
    }
}
