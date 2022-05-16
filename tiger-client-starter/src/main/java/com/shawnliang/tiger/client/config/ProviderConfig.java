package com.shawnliang.tiger.client.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description :   .
 *注册的信息
 * @author : Phoebe
 * @date : Created in 2022/5/16
 */
@Data
@EqualsAndHashCode
public class ProviderConfig {

    private String host;

    private Integer port;

    private String originUrl;

    private Integer weight = 100;

    private String version;

    /**
     * 序列化的方式（以服务端为准）
     */
    private String serializationType;

    private ConcurrentMap<String, String> staticAttrs = new ConcurrentHashMap<>();

    private ConcurrentMap<String, String> dynamicAttrs = new ConcurrentHashMap<>();



}
