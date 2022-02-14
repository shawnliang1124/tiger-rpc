package com.shawnliang.tiger.server.initialize;

import com.shawnliang.tiger.core.codec.TigerRpcServerDecoder;
import com.shawnliang.tiger.core.codec.TigerRpcServerEncoder;
import com.shawnliang.tiger.server.handler.TigerRpcServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    public static final String SPLIT_SYMBOL = "$_";

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ByteBuf splitBuffer = Unpooled.copiedBuffer(SPLIT_SYMBOL.getBytes());

        channel.pipeline().addLast(new DelimiterBasedFrameDecoder(62235, splitBuffer));
        channel.pipeline().addLast("encoder", new TigerRpcServerEncoder());
        channel.pipeline().addLast("decoder", new TigerRpcServerDecoder());
        channel.pipeline().addLast("handler", new TigerRpcServerHandler());
    }
}
