package com.shawnliang.tiger.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Data
@ConfigurationProperties(prefix = "tiger.rpc.client")
public class TigerRpcClientProperties {


    /**
     * 注册中心地址
     */
    private String discoveryAddr;

    /**
     * 超时时间
     */
    private Long timeout;

    /**
     * 注册的类型
     */
    private String registry;

    /**
     * 负载均衡路由
     */
    private String routeStrategy;

}
