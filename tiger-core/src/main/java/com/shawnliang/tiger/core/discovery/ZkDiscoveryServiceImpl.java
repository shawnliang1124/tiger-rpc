package com.shawnliang.tiger.core.discovery;

import com.shawnliang.tiger.core.common.ServiceInfo;
import com.shawnliang.tiger.core.route.IRoute;
import com.shawnliang.tiger.core.spi.TigerSpiImpl;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.util.CollectionUtils;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
@TigerSpiImpl(value = "zookeeper")
public class ZkDiscoveryServiceImpl implements DiscoveryService {

    public static final int BASE_SLEEP_TIME_MS = 1000;
    public static final int MAX_RETRIES = 3;
    public static final String ZK_BASE_PATH = "/tiger_rpc";

    private ServiceDiscovery<ServiceInfo> discovery;

    /**
     * 路由算法
     */
    private IRoute route;

    public ZkDiscoveryServiceImpl(String registryAddress, IRoute route) {
        this.route = route;

        try {
            CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddress,
                    new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
            client.start();

            JsonInstanceSerializer<ServiceInfo> serializer = new JsonInstanceSerializer<>(
                    ServiceInfo.class);

            ServiceDiscovery<ServiceInfo> discovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                    .client(client)
                    .serializer(serializer)
                    .basePath(ZK_BASE_PATH)
                    .build();
            this.discovery = discovery;
            this.discovery.start();
        } catch (Exception e) {
            log.error("discover occur error, exception msg: {}", e.getMessage(), e);
        }
    }

    @Override
    public ServiceInfo discovery(String serviceName) throws Exception {
        Collection<ServiceInstance<ServiceInfo>> serviceInstances = discovery
                .queryForInstances(serviceName);

        return CollectionUtils.isEmpty(serviceInstances) ? null :
                this.route.chooseOne(serviceInstances.stream().map(ServiceInstance::getPayload).collect(
                        Collectors.toList()));
    }
}
