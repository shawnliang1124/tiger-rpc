package com.shawnliang.tiger.client.initilize;

import com.shawnliang.tiger.client.handler.TigerRpcClientHandler;
import com.shawnliang.tiger.core.codec.TigerRpcClientDecoder;
import com.shawnliang.tiger.core.codec.TigerRpcClientEncoder;
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
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ByteBuf splitBuffer = Unpooled.copiedBuffer("$_".getBytes());
        channel.pipeline().addLast(new DelimiterBasedFrameDecoder(62235, splitBuffer));
        channel.pipeline().addLast("decoder", new TigerRpcClientDecoder());
        channel.pipeline().addLast("handler", new TigerRpcClientHandler());
        channel.pipeline().addLast("encoder", new TigerRpcClientEncoder());
    }
}
