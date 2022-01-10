package com.tiger.rpc.netty;

import com.tiger.rpc.netty.handler.NettyDecoderServerHandler;
import com.tiger.rpc.netty.handler.NettyEncoderServerHandler;
import com.tiger.rpc.netty.handler.NettyServerRpcHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/8
 */
public class NettyServerInitialize extends ChannelInitializer<SocketChannel> {

    public static final String SPLIT_SYMBOL = "$_";


    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ByteBuf splitBuffer = Unpooled.copiedBuffer(SPLIT_SYMBOL.getBytes());

        // 使用特殊符号结尾解决粘包拆包的问题
        channel.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, splitBuffer));
        channel.pipeline().addLast("decoder", new NettyDecoderServerHandler());
        channel.pipeline().addLast("encoder", new NettyEncoderServerHandler());
        channel.pipeline().addLast("handler", new NettyServerRpcHandler());

    }
}
