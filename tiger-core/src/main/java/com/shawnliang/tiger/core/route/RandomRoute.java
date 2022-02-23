package com.shawnliang.tiger.core.route;

import com.shawnliang.tiger.core.common.ServiceInfo;
import com.shawnliang.tiger.core.spi.TigerSpiImpl;
import java.util.List;
import java.util.Random;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/23
 */
@TigerSpiImpl(value = "random")
public class RandomRoute implements IRoute {

    @Override
    public ServiceInfo chooseOne(List<ServiceInfo> serviceInfoList) {
        int size = serviceInfoList.size();
        Random random = new Random(size);
        return serviceInfoList.get(random.nextInt());
    }
}
