package com.shawnliang.tiger.server.config;

import com.shawnliang.tiger.core.TigerConfigConstant;
import com.shawnliang.tiger.core.TigerConfigs;
import com.shawnliang.tiger.core.register.RegistryService;
import com.shawnliang.tiger.core.spi.TigerSpiClass;
import com.shawnliang.tiger.core.spi.TigerSpiClassLoaderFactory;
import com.shawnliang.tiger.server.transport.ITigerServer;
import com.shawnliang.tiger.server.transport.TigerNettyServer;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Configuration
@EnableConfigurationProperties(value = TigerRpcServerProperties.class)
public class TigerRpcServerAutoConfiguration {

    /**
     *  Server配置文件属性绑定 全局配置文件
     * @param environment
     * @return
     */
    @Bean
    public TigerRpcServerProperties serverProperties(Environment environment) {
        BindResult<TigerRpcServerProperties> result = Binder.get(environment)
                .bind("tiger.rpc.server", TigerRpcServerProperties.class);
        TigerRpcServerProperties properties = result.get();

        Optional.of(properties.getPort()).ifPresent(port ->
                TigerConfigs.putValue(TigerConfigConstant.SERVER_PORT, port));
        Optional.ofNullable(properties.getRegistryAddr()).ifPresent(addr ->
                TigerConfigs.putValue(TigerConfigConstant.SERVER_REGISTRY_ADDR, addr));

        return properties;
    }

    /**
     * 将当前应用注册到注册中心
     * 默认使用zk注册中心
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @DependsOn(value = {"serverProperties"})
    public RegistryService registryService() {
        TigerSpiClass<? extends RegistryService> spiClass = TigerSpiClassLoaderFactory
                .getSpiLoader(RegistryService.class)
                .getSpiClass(TigerConfigs.getDefaultString(TigerConfigConstant.REGISTRY_KEY));

        // 获得注册中心的地址
        String serverRegistryAddr = TigerConfigs
                .getDefaultString(TigerConfigConstant.SERVER_REGISTRY_ADDR);
        // 通过反射进行初始化
        RegistryService instance = spiClass
                .getInstance(new Class[]{String.class}, new Object[]{serverRegistryAddr});

        return instance;
    }

    @Bean
    @ConditionalOnMissingBean
    public ITigerServer tigerServer() {
        return new TigerNettyServer();
    }

    @Bean
    @DependsOn(value = {"serverProperties"})
    public TigerRpcProvider tigerRpcProvider(
            @Autowired RegistryService registryService,
            @Autowired ITigerServer iTigerServer,
            @Autowired TigerRpcServerProperties serverProperties) {
        return new TigerRpcProvider(iTigerServer, registryService, serverProperties);
    }


}
