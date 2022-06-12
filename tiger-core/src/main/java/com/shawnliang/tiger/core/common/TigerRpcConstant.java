package com.shawnliang.tiger.core.common;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
public class TigerRpcConstant {

    /**
     * 默认spi扫描的路径
     */
    public static final String[] SPI_BASE_PATHS =
            new String[]{"META-INF/tiger-rpc/"};


    public static final String ASYNC = "async";

    public static final String SYNC = "sync";

    public static final String ONE_WAY = "one_way";

    public static final String CALL_BACK = "callback";

    public static final String PROVIDER_CLIENT_CLOSE_TIME = "provider.client.close.time";

    public static final String CONN_STATUS = "conn_status";
    public static final String CONN_STATUS_ON = "on";
    public static final String CONN_STATUS_OFF = "off";


    public static final int DEFAULT_CONN_NUM = 1;
}
