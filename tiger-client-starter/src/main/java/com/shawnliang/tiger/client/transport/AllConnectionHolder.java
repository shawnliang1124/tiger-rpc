package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.client.config.ProviderConfig;
import com.shawnliang.tiger.core.TigerConfigs;
import com.shawnliang.tiger.core.common.TigerRpcConstant;
import com.shawnliang.tiger.core.spi.TigerSpiImpl;
import com.shawnliang.tiger.core.struct.ThreadNameFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/5/16
 */
@TigerSpiImpl(value = "all")
public class AllConnectionHolder implements ConnectionHolder {

    private static final Logger logger = LoggerFactory.getLogger(AllConnectionHolder.class);

    /**
     * 维护长连接
     */
    private Map<ProviderConfig, TigerRpcClientTransport> availableConnections = new ConcurrentHashMap<>();

    /**
     * 亚健康的长连接
     */
    private Map<ProviderConfig, TigerRpcClientTransport> subHealthConnections = new ConcurrentHashMap<>();

    /**
     * 重试的长连接
     */
    private Map<ProviderConfig, TigerRpcClientTransport> retryConnections = new ConcurrentHashMap<>();

    private volatile boolean isRemove = false;

    @Override
    public void closeAllConnections() {
        if (isRemove) {
            logger.warn("正在进行销毁的操作，请稍后");
            return;
        }

        synchronized (AllConnectionHolder.class) {
            isRemove = true;

            Map<ProviderConfig, TigerRpcClientTransport> removeClients = getRemoveClients();
            // 使用线程池进行回收
            int providerSize = removeClients.size();
            if (providerSize == 0) {
                logger.warn("无需进行销毁，provider list is 0");
                return;
            }

            int threads = Math.max(10, providerSize);
            Long destroyTime = TigerConfigs
                    .getDefaultLong(TigerRpcConstant.PROVIDER_CLIENT_CLOSE_TIME);
            if (destroyTime == null) {
                destroyTime = 10000L;
            }
            ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(threads, threads, 0,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingDeque<>(providerSize),
                    new ThreadNameFactory("ALLCONNECTION-DESTROY"));
            CountDownLatch countDownLatch = new CountDownLatch(providerSize);
            Long finalDestroyTime = destroyTime;

            removeClients.forEach((providerConfig, clientTransport) -> {
                poolExecutor.submit(() -> {
                    //  进行client的关闭
                    //  依旧存在的请求需要等待结束后再关闭
                    try {
                        ClientTransFactory.releaseTrans(clientTransport, finalDestroyTime);
                    } catch (Exception e) {
                        logger.error("atch exception but ignore it when close alive client", e);
                    } finally {
                        countDownLatch.countDown();
                    }

                });
            });

            try {
                long totalTimeout =
                        ((providerSize % threads == 0) ? (providerSize / threads) : ((providerSize /
                                threads) + 1)) * destroyTime + 500;
                countDownLatch.await(totalTimeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Exception when close transport", e);
            } finally {
                poolExecutor.shutdown();
            }
        }

    }

    @Override
    public TigerRpcClientTransport getAvailableClient(ProviderConfig providerConfig) {

        return null;
    }


    /**
     * 获得回收的client列表
     *
     * @return
     */
    private Map<ProviderConfig, TigerRpcClientTransport> getRemoveClients() {
        Map<ProviderConfig, TigerRpcClientTransport> removeClients = new HashMap<>(
                availableConnections);

        removeClients.putAll(subHealthConnections);
        removeClients.putAll(retryConnections);

        availableConnections.clear();
        subHealthConnections.clear();
        retryConnections.clear();

        return removeClients;
    }
}
