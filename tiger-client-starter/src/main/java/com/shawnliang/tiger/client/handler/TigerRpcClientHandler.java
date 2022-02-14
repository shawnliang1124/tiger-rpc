package com.shawnliang.tiger.client.handler;

import com.shawnliang.tiger.client.transport.TransportCache;
import com.shawnliang.tiger.core.common.TigerRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class TigerRpcClientHandler extends SimpleChannelInboundHandler<TigerRpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
            TigerRpcResponse tigerRpcResponse) throws Exception {
        // 拿到response 打印拦截
        log.info("response is: {}", tigerRpcResponse);
        TransportCache.fillResponse(tigerRpcResponse.getHeader().getRequestId(), tigerRpcResponse);
    }
}
