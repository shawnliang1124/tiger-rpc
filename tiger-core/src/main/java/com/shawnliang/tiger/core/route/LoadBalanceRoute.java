package com.shawnliang.tiger.core.route;

import com.shawnliang.tiger.core.common.ServiceInfo;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description :  负载均衡算法.
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class LoadBalanceRoute implements IRoute {

    private static final int start = 0;

    private AtomicInteger index = new AtomicInteger(start);

    @Override
    public ServiceInfo chooseOne(List<ServiceInfo> serviceInfoList) {
        int i = index.get();
        if (i >= serviceInfoList.size()) {
            index.set(start);
            return serviceInfoList.get(start);
        }

        ServiceInfo serviceInfo = serviceInfoList.get(i);
        index.incrementAndGet();

        return serviceInfo;
    }
}
