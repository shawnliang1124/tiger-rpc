package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.client.config.ProviderConfig;
import com.shawnliang.tiger.core.spi.TigerSpi;

/**
 * Description :  长连接维护器 .
 *
 * @author : Phoebe
 * @date : Created in 2022/5/16
 */
@TigerSpi
public interface ConnectionHolder {

    /**
     * 关闭所有的长连接
     */
    void closeAllConnections();


    /**
     * 获得存活的客户端
     * @return
     */
    TigerRpcClientTransport getAvailableClient(ProviderConfig providerConfig);

}
