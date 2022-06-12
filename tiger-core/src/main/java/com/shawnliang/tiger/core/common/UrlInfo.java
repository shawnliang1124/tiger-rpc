package com.shawnliang.tiger.core.common;

import java.util.Properties;
import lombok.Data;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/5/23
 */
@Data
public class UrlInfo {

    private String ip;

    private int port;

    private String address;

    private String protocol;

    /**
     * 地址的唯一键
     */
    private String uniKey;

    /**
     * 地址的超时时间
     */
    private long connectTimeout;

    private Properties properties;

    private int connNum;

    private boolean connWarmup;

    public UrlInfo(String ip, int port, String protocol) {
        this.ip = ip;
        this.port = port;
        this.protocol = protocol;

        // 唯一key = ip:端口
        this.uniKey = ip + ":" + port;
    }
}
