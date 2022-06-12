package com.shawnliang.tiger.core.connections;

import com.shawnliang.tiger.core.common.NettyConnection;
import com.shawnliang.tiger.core.common.UrlInfo;
import com.shawnliang.tiger.core.struct.ThreadNameFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :   异步去获取创建连接池.
 *
 * @author : Phoebe
 * @date : Created in 2022/6/12
 */
public class ConnectionPoolCall implements Callable<ConnectionPool> {

    /**
     * is need to init conn?
     */
    private boolean isInitConnection;

    private UrlInfo urlInfo;

    private ConnectSelectStrategy selectStrategy;

    private IConnectionFactory connectionFactory;

    /**
     * is async executor init
     */
    private volatile boolean executorInit;

    private volatile ExecutorService asyncInitExecutor;

    private volatile static boolean asyncInitMark = true;

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolCall.class);

    protected ConnectionPoolCall(UrlInfo urlInfo,
            ConnectSelectStrategy selectStrategy,
            IConnectionFactory connectionFactory) {
        this.urlInfo = urlInfo;
        this.isInitConnection = true;
        this.selectStrategy = selectStrategy;
        this.connectionFactory = connectionFactory;
    }



    @Override
    public ConnectionPool call() throws Exception {
        // init pool first
        ConnectionPool pool = new ConnectionPool(selectStrategy);
        if (isInitConnection) {
            doCreate(this.urlInfo, pool, this.getClass().getSimpleName(), 1);
        }


        return pool;
    }

    private void doCreate(UrlInfo urlInfo, ConnectionPool pool, String simpleName, int syncNotWarnUpConn)
            throws Exception {
        // actual conns in the pool
        int actualNum = pool.poolSize();
        int expectNum = urlInfo.getConnNum();

        if (actualNum >= expectNum) {
            if (logger.isDebugEnabled()) {
                logger.debug("actualNum num is enough!, actNum: {}, expectNum: {}", expectNum, actualNum);
            }
            return;
        }

        // 开始创建
        if (urlInfo.isConnWarmup()) {
            for (int i = actualNum; i < expectNum; ++i) {
                NettyConnection connection = connectionFactory.createNettyConnection(urlInfo);
                pool.addConn(connection);
            }
        } else {
            if (syncNotWarnUpConn > 0) {
                for (int i = 0; i < syncNotWarnUpConn; i++) {
                    NettyConnection conn = connectionFactory
                            .createNettyConnection(urlInfo);
                    pool.addConn(conn);
                }

                if (syncNotWarnUpConn == expectNum) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("not warn up num is equals expect num, do return");
                    }
                    return;
                }
            }

            // 线程池创建剩余的connection
            initAsyncExecutorLazy();

            // 执行创建connection的业务
            asyncInitMark = false;
            try {
                for (int i = pool.poolSize(); i < expectNum; i++) {
                    asyncInitExecutor.execute(() -> {
                        try {
                            NettyConnection nettyConnection = connectionFactory
                                    .createNettyConnection(urlInfo);
                            pool.addConn(nettyConnection);
                        } catch (Exception e) {
                            logger.error("async create conn error!", e);
                        }
                    });
                }
            } finally {
                asyncInitMark = true;
            }

        }
    }

    private void initAsyncExecutorLazy() {
        if (!executorInit) {
            this.executorInit = true;
            // todo 优化为配置线程池
            int threadNum = Runtime.getRuntime().availableProcessors();
            this.asyncInitExecutor = new ThreadPoolExecutor(threadNum, threadNum, 30, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(100), new ThreadNameFactory("warn-up-conn-thread", true));

        }
    }
}
