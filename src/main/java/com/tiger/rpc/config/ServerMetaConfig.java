package com.tiger.rpc.config;

import io.netty.util.internal.SystemPropertyUtil;
import lombok.Data;

/**
 * Description :存储server的相关属性   .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/8
 */
@Data
public class ServerMetaConfig {

    public static int DEFAULT_BOSS_GROUP_NUM;

    public static int DEFAULT_WORKER_GROUP_NUM;

    static {
        DEFAULT_BOSS_GROUP_NUM  = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
        DEFAULT_WORKER_GROUP_NUM = DEFAULT_BOSS_GROUP_NUM * 2;
    }

    /**
     * 端口号
     */
    private Integer port;

    /**
     * boss eventloopgroup 可配置线程数
     */
    private int bossGroupThread;

    /**
     * worker eventloopgroup可配置的线程数
     */
    private int workerGroupThread;

    private ProviderMeta providerMeta;

    public ServerMetaConfig(ProviderMeta providerMeta) {
        this.providerMeta = providerMeta;
    }
}
