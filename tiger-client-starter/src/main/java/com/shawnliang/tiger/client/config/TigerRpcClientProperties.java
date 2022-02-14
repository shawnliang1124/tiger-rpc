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


    private String discoveryAddr;

    private Long timeout;

}
