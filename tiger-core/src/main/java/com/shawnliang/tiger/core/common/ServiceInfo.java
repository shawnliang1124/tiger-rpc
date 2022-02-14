package com.shawnliang.tiger.core.common;

import lombok.Data;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Data
public class ServiceInfo {

    private String appName;

    private String serviceName;

    private String version;

    private String address;

    private Integer port;
}
