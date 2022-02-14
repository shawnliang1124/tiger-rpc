package com.shawnliang.tiger.core.register;

import com.shawnliang.tiger.core.common.ServiceInfo;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class ZkRegistryServiceImpl implements RegistryService {
    public static final int BASE_SLEEP_TIME_MS = 1000;
    public static final int MAX_RETRIES = 3;
    public static final String ZK_BASE_PATH = "/tiger_rpc";

    private ServiceDiscovery<ServiceInfo> discovery;

    /**
     * @param registryAddress 注册中心地址
     */
    public ZkRegistryServiceImpl(String registryAddress) {
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
            log.error("registry occur error, exception msg: {}", e.getMessage(), e);
        }
    }

    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                .name(serviceInfo.getServiceName())
                .address(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .payload(serviceInfo)
                .build();

        this.discovery.registerService(serviceInstance);

    }

    @Override
    public void unRegister(ServiceInfo serviceInfo) throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                .name(serviceInfo.getServiceName())
                .address(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .payload(serviceInfo)
                .build();

        this.discovery.unregisterService(serviceInstance);
    }

    @Override
    public void destroy() throws IOException {
        this.discovery.close();
    }
}
