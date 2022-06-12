package com.shawnliang.tiger.core.connections;

import com.shawnliang.tiger.core.common.NettyConnection;
import com.shawnliang.tiger.core.common.UrlInfo;
import com.shawnliang.tiger.core.enums.ConnectionEventType;
import com.shawnliang.tiger.core.exception.RpcException;
import com.shawnliang.tiger.core.struct.ThreadNameFactory;
import com.shawnliang.tiger.core.utils.RunStateFutureTask;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

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

    private static final LongAdder requestCount = new LongAdder();

    private ConnectSelectStrategy selectStrategy;

    protected ConcurrentHashMap<String, RunStateFutureTask<ConnectionPool>> connTasks;

    private static final long FUTURE_WAIT_TIME = 30000L;

    private static final int RETRY_TIME = 3;

    private static final Logger logger = LoggerFactory.getLogger(AbsConnectionFactory.class);

    public AbsConnectionFactory(ChannelHandler heartBeatHandler, ChannelHandler handler,
            ChannelHandler encoder, ChannelHandler decoder, ConnectSelectStrategy selectStrategy) {
//        if (heartBeatHandler == null) {
//            throw new RpcException("heartBeatHandler must not be null");
//        }

        initProperties();

        if (handler == null) {
            throw new RpcException("channelHandler must not be null");
        }

        this.heartbeatHandler = heartBeatHandler;
        this.handler = handler;
        this.encoder = encoder;
        this.decoder = decoder;
        this.selectStrategy = selectStrategy;
    }

    private void initProperties() {
        this.connTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void init(ConnectionEventHandler connectionEventHandler) {
        bootstrap = new Bootstrap();
        // 获得CPU数量
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadNameFactory threadNameFactory = new ThreadNameFactory("tiger-rpc-", true);

        eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup(processors, threadNameFactory)
                : new NioEventLoopGroup(processors, threadNameFactory);

        bootstrap.group(eventLoopGroup)
                .channel(eventLoopGroup instanceof EpollEventLoopGroup ?
                        EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ByteBuf splitBuffer = Unpooled.copiedBuffer("$_".getBytes());
                        channel.pipeline().addLast(new DelimiterBasedFrameDecoder(62235, splitBuffer));
                        channel.pipeline().addLast("decoder", decoder);
                        channel.pipeline().addLast("encoder", encoder);
                        channel.pipeline().addLast("handler", handler);
                        Optional.ofNullable(heartbeatHandler).ifPresent(hb -> channel.pipeline().addLast("heartBeat", hb));
//                        channel.pipeline().addLast("conncetion", connectionEventHandler);

                    }
                });

        // todo 设置高低水位

        logger.info("init client channel finished!");
    }

    @Override
    public NettyConnection createNettyConnection(UrlInfo urlInfo) throws Exception {
        Channel channel = this.doCreateConnect(urlInfo.getIp(), urlInfo.getPort(), urlInfo.getConnectTimeout());
        NettyConnection connection = new NettyConnection(urlInfo, channel);

        if (channel.isActive()) {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT);
        } else {
            channel.pipeline().fireUserEventTriggered(ConnectionEventType.CONNECT_FAILED);
        }

        return connection;

    }


    @Override
    public NettyConnection getAndCreateIfNotExist(UrlInfo urlInfo) {
        // 从连接池中获取
        ConnectionPool pool;
        try {
             pool = getPoolAndCreateIfNotExist(urlInfo.getUniKey(),
                    new ConnectionPoolCall(urlInfo, selectStrategy, this));
        } catch (Exception e) {
            logger.error("get netty connect error", e);
            throw new RpcException("get connect error!");
        }

        return pool.getConn();
    }

    @Override
    public void check(NettyConnection connection) {
        if (connection == null) {
            throw new RpcException("connection is null");
        }
        if (connection.getChannel() == null || !connection.getChannel().isActive()) {
            //  close the invalid connection
            remove(connection);
        }

        if (!connection.getChannel().isWritable()) {
            throw new RpcException("channel can't be written");
        }
    }

    private void remove(NettyConnection connection) {
        if (connection == null) {
            return;
        }

        Set<String> poolKeys = connection.getPoolKeys();
        if (CollectionUtils.isEmpty(poolKeys)) {
            connection.close();
        } else {
            for (String poolKey : poolKeys) {
                remove(connection, poolKey);
            }
        }

    }

    private void remove(NettyConnection connection, String poolKey) {
        if (connection == null || StringUtils.isBlank(poolKey)) {
            return;
        }

        // 拿到future task
        RunStateFutureTask<ConnectionPool> futureTask = this.connTasks.get(poolKey);
        ConnectionPool pool = null;
        try {
             pool = futureTask.getAfterRun();
        } catch (Exception e) {
            logger.error("future get pool error", e);
        }

        if (pool == null) {
            connection.close();
        } else{
            // close the conn in the pool
            pool.removeAndTryClose(connection);
            if (pool.isEmpty()) {
                RunStateFutureTask<ConnectionPool> removeTask = this.connTasks.remove(poolKey);
                if (removeTask != null) {
                    try {
                        pool = futureTask.getAfterRun();
                    } catch (Exception e) {
                        logger.error("future get pool error", e);
                    }

                    if (pool != null) {
                        pool.removeAllAndClose();
                    }
                }
            }
        }



    }

    /**
     * 异步去获取线程池
     * @param poolKey
     * @param connectionPoolCall
     * @return
     */
    private ConnectionPool getPoolAndCreateIfNotExist(String poolKey, Callable<ConnectionPool> connectionPoolCall)
            throws Exception {
        RunStateFutureTask<ConnectionPool> task = connTasks.get(poolKey);
        if (task == null) {
            task = new RunStateFutureTask<>(connectionPoolCall);
            RunStateFutureTask<ConnectionPool> beforeTask = connTasks
                    .putIfAbsent(poolKey, task);

            // previous value is null, run call method at once
            if (beforeTask == null) {
                task.run();
            }
        }

        // future task impossibly not be null here
        ConnectionPool pool = task.get();
        int nullCount = 0;
        int interruptCount = 0;
        if (pool == null) {
            for (int i = 0; i < RETRY_TIME; i++) {
                try {
                    pool = task.get(FUTURE_WAIT_TIME, TimeUnit.MICROSECONDS);
                    if (pool != null) {
                        break;
                    }

                    nullCount++;
                } catch (InterruptedException e) {
                   logger.warn("interrupt by other thread");
                   if (i + 1 < RETRY_TIME) {
                       interruptCount++;
                       continue;
                   }

                   connTasks.remove(poolKey);
                   logger.warn("future task interrupt: poolKey: {}, interruptCount time: {}" , poolKey, interruptCount);
                   throw e;
                } catch (Exception e) {
                    connTasks.remove(poolKey);
                    logger.error("future task get exception!", e);
                }
            } // end for loop

            // loop end, but pool is still not exist
            if (pool == null) {
                connTasks.remove(poolKey);
            }
        }

        if (nullCount > 0) {
            logger.warn("create pool is slow, please check!, nullCount: {}", nullCount);
        }

        return pool;
    }

    private Channel doCreateConnect(String ip, int port, long timeout) throws Exception {
        long realTimeout = Math.max(timeout, 1000L);

        if (logger.isDebugEnabled()) {
            logger.debug("start connect new netty channel, ip: {}, port: {}", ip, port);
        }

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) realTimeout);
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
