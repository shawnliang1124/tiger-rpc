package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.client.initilize.NettyClientInitializer;
import com.shawnliang.tiger.core.common.NettyConnection;
import com.shawnliang.tiger.core.common.UrlInfo;
import com.shawnliang.tiger.core.enums.ConnectionEventType;
import com.shawnliang.tiger.core.exception.RpcException;
import com.shawnliang.tiger.core.struct.ThreadNameFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/5/23
 */
public abstract class AbsConnectionFactory implements IConnectionFactory{

    private Bootstrap bootstrap;

    private EventLoopGroup eventLoopGroup;

    private final ChannelHandler encoder;

    private final ChannelHandler decoder;

    private final ChannelHandler heartbeatHandler;

    private final ChannelHandler handler;

    private static final AtomicLong requestCount = new AtomicLong(0);

    private static final Logger logger = LoggerFactory.getLogger(AbsConnectionFactory.class);

    public AbsConnectionFactory(ChannelHandler heartBeatHandler, ChannelHandler handler,
            ChannelHandler encoder, ChannelHandler decoder) {
        if (heartBeatHandler == null) {
            throw new RpcException("heartBeatHandler must not be null");
        }

        if (handler == null) {
            throw new RpcException("channelHandler must not be null");
        }

        this.heartbeatHandler = heartBeatHandler;
        this.handler = handler;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public void init(ConnectionEventHandler connectionEventHandler) {
        bootstrap = new Bootstrap();
        // 获得CPU数量
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadNameFactory threadNameFactory = new ThreadNameFactory("tiger-pubstuff-", true);

        eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(processors, threadNameFactory)
                : new NioEventLoopGroup(processors, threadNameFactory);

        bootstrap.group(eventLoopGroup)
                .channel(eventLoopGroup instanceof EpollEventLoopGroup ?
                        EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new NettyClientInitializer(encoder, decoder, heartbeatHandler, handler, connectionEventHandler));

        // todo 设置高低水位

        logger.info("init client channel finished!");
    }

    @Override
    public NettyConnection createNettyConnection(String ip, int port, int timeout)
            throws Exception {
        Channel channel = this.doCreateConnect(ip, port, timeout);
        UrlInfo urlInfo = new UrlInfo(ip, port, "");
        NettyConnection connection = new NettyConnection(urlInfo, channel);

        if (channel.isActive()) {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        } else {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT_FAILED);
        }

        return connection;
    }

    private Channel doCreateConnect(String ip, int port, int timeout) throws Exception {
        int realTimeout = Math.max(timeout, 1000);

        if (logger.isDebugEnabled()) {
            logger.debug("start connect new netty channel, ip: {}, port: {}", ip, port);
        }

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, realTimeout);
        ChannelFuture channelFuture = bootstrap.connect(ip, port);

        // 阻塞等待， 连接
        channelFuture.awaitUninterruptibly();

        if (!channelFuture.isDone()) {
            String errorMsg = "connect is timeout, not finished in: (%s), ip:(%s), port:(%s)";
            logger.warn(String.format(errorMsg, timeout, ip, port));
            throw new Exception(String.format(errorMsg, timeout, ip, port), channelFuture.cause());

        }

        if (channelFuture.isCancelled()) {
            String errorMsg = "connect is cancelled by user!, ip:%s, port: %s";
            logger.warn(String.format(errorMsg, ip, port));
            throw new Exception(String.format(errorMsg, ip, port), channelFuture.cause());
        }

        if (!channelFuture.isSuccess()) {
            String errorMsg = "connect is not success, ip:(%s), port:(%s)";
            logger.warn(String.format(errorMsg, ip, port));
            throw new Exception(String.format(errorMsg, ip, port), channelFuture.cause());
        }

        // 创建连接成功
        return channelFuture.channel();
    }
}
