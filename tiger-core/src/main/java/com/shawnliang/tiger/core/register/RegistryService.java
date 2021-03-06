package com.shawnliang.tiger.core.register;

import com.shawnliang.tiger.core.common.ServiceInfo;
import com.shawnliang.tiger.core.spi.TigerSpi;
import java.io.IOException;

/**
 * Description : 服务注册发现  .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@TigerSpi
public interface RegistryService {

    void register(ServiceInfo serviceInfo) throws Exception;

    void unRegister(ServiceInfo serviceInfo) throws Exception;

    void destroy() throws IOException;

}
