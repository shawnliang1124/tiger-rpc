package com.shawnliang.tiger.client.initilize;

import com.shawnliang.tiger.client.transport.ConnectionEventHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelHandler encoder;

    private final ChannelHandler decoder;

    private final ChannelHandler heartbeatHandler;

    private final ChannelHandler handler;

    private final ConnectionEventHandler connectionEventHandler;


    public NettyClientInitializer(
            ChannelHandler encoder,
            ChannelHandler decoder,
            ChannelHandler heartBeat,
            ChannelHandler handler,
            ConnectionEventHandler connectionEventHandler
            ) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.heartbeatHandler = heartBeat;
        this.handler = handler;

        this.connectionEventHandler = connectionEventHandler;

    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ByteBuf splitBuffer = Unpooled.copiedBuffer("$_".getBytes());
        channel.pipeline().addLast(new DelimiterBasedFrameDecoder(62235, splitBuffer));
        channel.pipeline().addLast("decoder", decoder);
        channel.pipeline().addLast("handler", handler);
        channel.pipeline().addLast("heartBeat", heartbeatHandler);
        channel.pipeline().addLast("conncetion", connectionEventHandler);
        channel.pipeline().addLast("encoder", encoder);
    }
}
