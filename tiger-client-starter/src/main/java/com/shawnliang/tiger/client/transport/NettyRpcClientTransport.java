package com.shawnliang.tiger.client.transport;

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
import java.util.concurrent.atomic.AtomicLong;
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

    private static final AtomicLong requestCount = new AtomicLong(0);

    public NettyRpcClientTransport() {
        bootstrap = new Bootstrap();
        eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        bootstrap.group(eventLoopGroup)
                .channel(eventLoopGroup instanceof EpollEventLoopGroup ?
                        EpollSocketChannel.class : NioSocketChannel.class);
//                .handler(new NettyClientInitializer(handler));

    }



    @Override
    public TigerRpcResponse sendRequest(TransMetaInfo transMetaInfo) throws Exception {
        TigerRpcResponseFuture<TigerRpcResponse> future = doSendRequest(transMetaInfo);

        // future.get，阻塞调用线程，等待结果的返回
        if (transMetaInfo.getTimeout() == null) {
            return future.get(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } else {
            return future.get(transMetaInfo.getTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void sendRequestAsync(TransMetaInfo transMetaInfo) throws Exception {
        // 发送完请求直接结束，释放调用线程
        TigerRpcResponseFuture<TigerRpcResponse> future = doSendRequest(transMetaInfo);
        log.info("异步请求，直接返回。。future: {}", future);
    }

    @Override
    public void connect() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isAvaliable() {
        return false;
    }

    @Override
    public int getCurrentCount() {
        return 0;
    }

    private TigerRpcResponseFuture<TigerRpcResponse> doSendRequest(TransMetaInfo transMetaInfo)
            throws InterruptedException {
        // 写入，并且等待该请求
        TigerRpcResponseFuture<TigerRpcResponse> future = new TigerRpcResponseFuture<>();
        TransportCache.add(transMetaInfo.getRequest().getHeader().getRequestId(), future);

       doSendByNetty(transMetaInfo);

        requestCount.incrementAndGet();

        return future;
    }

    private void doSendByNetty(TransMetaInfo transMetaInfo) throws InterruptedException {
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

    }
}
