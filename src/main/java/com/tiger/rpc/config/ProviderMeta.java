package com.tiger.rpc.config;

import lombok.Data;

/**
 * Description : 服务提供者支持的可配置变量  .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/9
 */
@Data
public class ProviderMeta {

    private String zkPath;

    private Integer port;

    private String ip;

    private String service;

    private String env;

    private String serverType;

    private String version;

}
