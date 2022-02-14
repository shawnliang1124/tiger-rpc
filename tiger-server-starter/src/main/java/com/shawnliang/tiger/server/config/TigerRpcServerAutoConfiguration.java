package com.shawnliang.tiger.server.config;

import com.shawnliang.tiger.core.register.RegistryService;
import com.shawnliang.tiger.core.register.ZkRegistryServiceImpl;
import com.shawnliang.tiger.server.transport.ITigerServer;
import com.shawnliang.tiger.server.transport.TigerNettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Configuration
@EnableConfigurationProperties(value = TigerRpcServerProperties.class)
public class TigerRpcServerAutoConfiguration {

    @Autowired
    private TigerRpcServerProperties serverProperties;

    /**
     * 将当前应用注册到注册中心
     * 默认使用zk注册中心
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public RegistryService registryService() {
        return new ZkRegistryServiceImpl(serverProperties.getRegistryAddr());
    }

    @Bean
    @ConditionalOnMissingBean
    public ITigerServer tigerServer() {
        return new TigerNettyServer();
    }

    @Bean
    public TigerRpcProvider tigerRpcProvider(@Autowired RegistryService registryService,
            @Autowired ITigerServer iTigerServer) {
        return new TigerRpcProvider(iTigerServer, registryService, serverProperties);
    }


}
