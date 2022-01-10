package com.tiger.rpc.netty;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.tiger.rpc.config.ProviderMeta;
import com.tiger.rpc.config.ServerMetaConfig;
import com.tiger.rpc.constant.TigerRpcConstant;
import com.tiger.rpc.register.zk.ZkConfig;
import com.tiger.rpc.register.zk.ZkServerRegister;
import com.tiger.rpc.server.IServer;
import com.tiger.rpc.utils.IPUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/8
 */
public class NettyServer implements IServer {

    private Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private ServerMetaConfig serverMetaConfig;

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    ZkServerRegister zkServerRegister;

    private static volatile boolean STOP = false;

    private static final long SHUT_DOWN_WAIT_TIME = 3000L;

    public NettyServer(ServerMetaConfig serverMetaConfig) {
        this.serverMetaConfig = serverMetaConfig;
    }

    @Override
    public void start() {
        try {
            if (Epoll.isAvailable()) {
                boss = new EpollEventLoopGroup(serverMetaConfig.getBossGroupThread() == 0 ?
                        ServerMetaConfig.DEFAULT_BOSS_GROUP_NUM : serverMetaConfig.getBossGroupThread());
                worker = new EpollEventLoopGroup(serverMetaConfig.getWorkerGroupThread() == 0 ?
                        ServerMetaConfig.DEFAULT_WORKER_GROUP_NUM: serverMetaConfig.getWorkerGroupThread());
            } else {
                boss = new NioEventLoopGroup(serverMetaConfig.getBossGroupThread() == 0 ?
                        ServerMetaConfig.DEFAULT_BOSS_GROUP_NUM : serverMetaConfig.getBossGroupThread());
                worker = new NioEventLoopGroup(serverMetaConfig.getWorkerGroupThread() == 0?
                        ServerMetaConfig.DEFAULT_WORKER_GROUP_NUM : serverMetaConfig.getWorkerGroupThread());
            }

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(worker instanceof EpollEventLoopGroup ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .handler ( new LoggingHandler( LogLevel.INFO ) )
                    .childHandler(new NettyServerInitialize())
                    .option (ChannelOption.SO_BACKLOG, 1024 )
                    .option (ChannelOption.SO_REUSEADDR, true )
                    .option (ChannelOption.SO_KEEPALIVE, true );

            bootstrap.bind(serverMetaConfig.getPort()).sync().channel();

            doWhenShutDown();

            // 注册
            doRegister();

        } catch (InterruptedException e) {
            logger.error("init server error", e);
        }
    }

    private void doWhenShutDown() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopNettyServer));
    }


    @Override
    public void stop() {
        stopNettyServer();
    }

    private void stopNettyServer() {
        if (!STOP) {
            if (zkServerRegister != null) {
                zkServerRegister.destroy();
            }

            try {
                Thread.sleep(SHUT_DOWN_WAIT_TIME);
            } catch (InterruptedException e) {
            }

            if (boss != null) {
                boss.shutdownGracefully();
            }
            if (worker != null) {
                worker.shutdownGracefully();
            }

            logger.info("netty server shut down finally, zkConfig: [{}]", serverMetaConfig);
            STOP = true;
        }

        while (!STOP) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("netty server stop waiting: [{}]", serverMetaConfig);
            }
        }

    }

    /**
     * 进行zk注册
     */
    private void doRegister() {
        ProviderMeta providerMeta = serverMetaConfig.getProviderMeta();
        if (providerMeta == null || StringUtils.isEmpty(providerMeta.getZkPath())) {
            logger.info("tiger netty server needn't register, cause provider is {}", providerMeta);
            return;
        }

        // 构建zkconfig对象
        ZkConfig zkConfig = ZkConfig.builder().zkPath(providerMeta.getZkPath())
                .env(providerMeta.getEnv())
                .port(providerMeta.getPort())
                .service(providerMeta.getService())
                .serverType(TigerRpcConstant.NETTY_SERVER_TYPE)
                .ip(IPUtil.getIpV4())
                .build();

        zkServerRegister = new ZkServerRegister(zkConfig);
        zkServerRegister.init();
    }
}
