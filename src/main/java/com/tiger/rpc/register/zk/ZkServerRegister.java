package com.tiger.rpc.register.zk;

import com.alibaba.fastjson.JSONObject;
import com.tiger.rpc.utils.IPUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/9
 */
public class ZkServerRegister {

    private final ZkConfig zkConfig;

    /**
     * 超时时间
     */
    private static final int TIMEOUT = 3000;

    /**
     * zk 重试次数
     */
    private static final int RETRY_TIMES = 2;

    private volatile ZooKeeper zookeeper;

    private static final Logger logger = LoggerFactory.getLogger(ZkServerRegister.class);

    private static volatile boolean initFlag = false;


    public ZkServerRegister(ZkConfig zkConfig) {
        if (zkConfig == null) {
            throw new IllegalArgumentException("zkconfig is not allowed null!");
        }
        this.zkConfig = zkConfig;
    }

    public void init() {
        String zkPath = zkConfig.getZkPath();

        CountDownLatch c = new CountDownLatch(1);
        if (zookeeper == null) {
            synchronized (ZkServerRegister.class) {
                if (zookeeper == null) {
                    try {
                        // 目录  zkPath/*
                        zookeeper = new ZooKeeper(zkPath, TIMEOUT, new TigerZkWatcher(c));
                    } catch (IOException e) {
                        logger.error("build zk occur error", e);
                    }

                    if (zookeeper == null) {
                        logger.error("init zk error, wait to retry {} times", RETRY_TIMES);
                    }

                    try {
                        int retry = 0;
                        boolean retryFlag = false;
                        while (retry++ < RETRY_TIMES) {
                            if (c.await(5, TimeUnit.SECONDS)) {
                                retryFlag = true;
                                break;
                            }
                        }

                        if (!retryFlag) {
                            logger.error("init zk registry error");
                            throw new RuntimeException("init zk error, zkConfig is " + zkConfig);
                        }
                    } catch (Exception e) {
                        logger.error("await error", e);
                    }

                    // 构建子目录
                    generateChildPath();
                }
            }
        }
    }

    /**
     * 构建子目录
     */
    private void generateChildPath() {
        String env = zkConfig.getEnv();
        String service = zkConfig.getService();

        try {
            env = env.startsWith("/") ? env : "/".concat(env);

            // zk文件目录 , zkPath/env/*
            if (zookeeper.exists(env, null) == null) {
                zookeeper.create(env, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            // zk文件目录, zkPath/env/server/*
            if (zookeeper.exists(service, null) == null) {
                zookeeper.create(service, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            JSONObject jsonObject = new JSONObject(true);
            jsonObject.put("ip", zkConfig.getIp());
            jsonObject.put("port", zkConfig.getPort());
            jsonObject.put("version", zkConfig.getVersion());
            jsonObject.put("sererType", zkConfig.getServerType());
            String childPathNodeData = jsonObject.toJSONString();

            String childPath = env + service + "/" + zkConfig.getIp() + ":" + zkConfig.getPort();
            if (zookeeper.exists(childPath, null) == null) {
                zookeeper.create(childPath, childPathNodeData.getBytes(StandardCharsets.UTF_8),
                        Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            }

            logger.info("zk server init success, info is: [{}]", zkConfig);

        } catch (Exception e) {
            logger.error("generate zk child path occur error", e);
            throw new RuntimeException("generate zk child path error");
        }
    }

    /**
     * 关闭zk 连接
     */
    public synchronized void destroy() {
        if (zookeeper != null) {
            try {
                zookeeper.close();
                initFlag = false;
                zookeeper = null;
            } catch (InterruptedException e) {
                logger.error("zk");
            }
        }
    }

    private class TigerZkWatcher implements Watcher {

        private CountDownLatch countDownLatch;

        public TigerZkWatcher(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            switch (watchedEvent.getState()) {
                case SyncConnected:
                    logger.info("the server {} is syncConnected", IPUtil.getIpV4());
                    countDownLatch.countDown();
                    break;
                case Expired:
                    logger.info("the server {} is expired, try reconnected", IPUtil.getIpV4());
                    reconnectedServer();
                    break;
                case Disconnected:
                    logger.info("the server {} is Disconnected!", IPUtil.getIpV4());
                    break;
                default:
                    logger.info("watch event not need to handler");
                    break;
            }

        }

        /**
         * 重新连接server
         */
        private void reconnectedServer() {
            ZkServerRegister.this.destroy();

            try {
                Thread.sleep(3000L);
            } catch (Exception e) {
                logger.error("reconnected error", e);
            }

            ZkServerRegister.this.init();

        }
    }

}
