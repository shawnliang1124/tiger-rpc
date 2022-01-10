package com.tiger.rpc.register.zk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZkConfig {
    private String zkPath;

    private Integer port;

    private String ip;

    private String env;

    private String serverType;

    private String version;


    private String service;
}
