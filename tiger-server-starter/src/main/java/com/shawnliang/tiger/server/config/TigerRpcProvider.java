package com.shawnliang.tiger.server.config;

import com.shawnliang.tiger.core.common.ServiceInfo;
import com.shawnliang.tiger.core.register.RegistryService;
import com.shawnliang.tiger.server.annonations.TigerRpcService;
import com.shawnliang.tiger.server.store.LocalCacheManager;
import com.shawnliang.tiger.server.transport.ITigerServer;
import java.io.IOException;
import java.net.InetAddress;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class TigerRpcProvider implements BeanPostProcessor, CommandLineRunner {

    private ITigerServer iTigerServer;

    private RegistryService registryService;

    private TigerRpcServerProperties properties;

    public TigerRpcProvider(ITigerServer iTigerServer,
            RegistryService registryService,
            TigerRpcServerProperties properties) {
        this.iTigerServer = iTigerServer;
        this.registryService = registryService;
        this.properties = properties;
    }

    @Override
    public void run(String... args) throws Exception {
        // 启动netty server
        new Thread(() -> {
            iTigerServer.start(properties.getPort());
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {

                // netty 关闭
                iTigerServer.stop();

                // 从注册中心上移除
                registryService.destroy();
            } catch (IOException e) {
                log.error("destroy failed, exception is", e);
            }
        }));
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        TigerRpcService tigerRpcService = bean.getClass().getAnnotation(TigerRpcService.class);
        try {
            if (tigerRpcService != null) {
                String serviceName = tigerRpcService.interfaceType().getName();
                String version = tigerRpcService.version();
                String serverName = StringUtils.join(serviceName, "_", version);
                LocalCacheManager.saveCache(serverName, bean);

                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.setServiceName(serverName);
                serviceInfo.setAddress(InetAddress.getLocalHost().getHostAddress());
                serviceInfo.setPort(properties.getPort());
                serviceInfo.setAppName(properties.getAppName());
                serviceInfo.setVersion(version);

                registryService.register(serviceInfo);
            }
        } catch (Exception e) {
            log.error("TigerRpcProvider init error", e);
        }

        return bean;
    }
}
