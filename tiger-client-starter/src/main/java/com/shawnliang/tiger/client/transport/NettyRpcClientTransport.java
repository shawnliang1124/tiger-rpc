package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.client.initilize.NettyClientInitializer;
import com.shawnliang.tiger.core.common.TigerRpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class NettyRpcClientTransport implements TigerRpcClientTransport{

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClientTransport() {
        bootstrap = new Bootstrap();
        eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        bootstrap.group(eventLoopGroup)
                .channel(eventLoopGroup instanceof EpollEventLoopGroup ?
                        EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new NettyClientInitializer());

    }

    @Override
    public TigerRpcResponse sendRequest(TransMetaInfo transMetaInfo) throws Exception {
        // 写入，并且等待该请求
        TigerRpcResponseFuture<TigerRpcResponse> future = new TigerRpcResponseFuture<>();
        TransportCache.add(transMetaInfo.getRequest().getHeader().getRequestId(), future);

        ChannelFuture channelFuture = bootstrap
                .connect(transMetaInfo.getAddress(), transMetaInfo.getPort()).sync();
        channelFuture.addListener((cl) -> {
            if (cl.isSuccess()) {
                log.info("netty tcp connect success, address: {}, port: {}",
                        transMetaInfo.getAddress(), transMetaInfo.getPort());
            } else {
                log.info("netty tcp connect failed, address: {}, port: {}",
                        transMetaInfo.getAddress(), transMetaInfo.getPort());
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });

        // 写入数据请求
        channelFuture.channel().writeAndFlush(transMetaInfo.getRequest());

        if (transMetaInfo.getTimeout() == null) {
            return future.get();
        } else {
            return future.get(transMetaInfo.getTimeout(), TimeUnit.MILLISECONDS);
        }
    }
}
