package com.shawnliang.tiger.core.discovery;

import com.shawnliang.tiger.core.common.ServiceInfo;
import com.shawnliang.tiger.core.spi.TigerSpi;

/**
 * Description :  注册 .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@TigerSpi
public interface DiscoveryService {

    /**
     * 寻找服务
     * @param serviceName
     * @return
     */
    ServiceInfo discovery(String serviceName) throws Exception;

}
