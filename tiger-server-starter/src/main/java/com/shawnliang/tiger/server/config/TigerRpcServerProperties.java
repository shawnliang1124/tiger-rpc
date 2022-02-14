package com.shawnliang.tiger.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Data
@ConfigurationProperties(prefix = "tiger.rpc.server")
public class TigerRpcServerProperties {

    /**
     * server启动的端口
     */
    private Integer port = 8090;

    /**
     * server的名字
     */
    private String appName;

    /**
     * 注册中心
     * (zk，consul等)地址
     */
    private String registryAddr = "127.0.0.1:2181";

}
