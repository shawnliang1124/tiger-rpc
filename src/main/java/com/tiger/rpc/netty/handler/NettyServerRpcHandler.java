package com.tiger.rpc.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :  请求处理类 .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/9
 */
public class NettyServerRpcHandler extends SimpleChannelInboundHandler<Object> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerRpcHandler.class);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // todo处理server的逻辑请求
    }
}
