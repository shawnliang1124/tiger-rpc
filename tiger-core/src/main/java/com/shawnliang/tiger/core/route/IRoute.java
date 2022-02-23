package com.shawnliang.tiger.core.route;

import com.shawnliang.tiger.core.common.ServiceInfo;
import com.shawnliang.tiger.core.spi.TigerSpi;
import java.util.List;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@TigerSpi
public interface IRoute {

    /**
     * 选择合适的算法去找到一个 serviceInfo对象
     * @param serviceInfoList
     * @return
     */
    ServiceInfo chooseOne(List<ServiceInfo> serviceInfoList);

}
