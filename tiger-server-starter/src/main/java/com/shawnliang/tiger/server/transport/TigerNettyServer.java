package com.shawnliang.tiger.server.transport;

import com.shawnliang.tiger.server.initialize.NettyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import java.net.InetAddress;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class TigerNettyServer implements ITigerServer {

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    @Override
    public void start(int port) {
        try {
            if (Epoll.isAvailable()) {
                boss = new EpollEventLoopGroup();
                worker = new EpollEventLoopGroup();
            } else {
                boss = new NioEventLoopGroup();
                worker = new NioEventLoopGroup();
            }

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(worker instanceof EpollEventLoopGroup ? EpollServerSocketChannel.class
                            : NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new NettyServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            ChannelFuture future = bootstrap.bind(hostAddress, port).sync();
            log.info("netty server start success on port:{} ", port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("init netty server error", e);
        } finally {
            stop();
        }

    }

    @Override
    public void stop() {
        Optional.ofNullable(boss).ifPresent(EventExecutorGroup::shutdownGracefully);
        Optional.ofNullable(worker).ifPresent(EventExecutorGroup::shutdownGracefully);

        log.info("stop netty server success!");
    }
}
