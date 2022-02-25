package com.shawnliang.tiger.client.config;

import com.shawnliang.tiger.client.proxy.ClientStubProxyFactory;
import com.shawnliang.tiger.client.transport.NettyRpcClientTransport;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.core.TigerConfigConstant;
import com.shawnliang.tiger.core.TigerConfigs;
import com.shawnliang.tiger.core.discovery.DiscoveryService;
import com.shawnliang.tiger.core.route.IRoute;
import com.shawnliang.tiger.core.spi.TigerSpiClass;
import com.shawnliang.tiger.core.spi.TigerSpiClassLoaderFactory;
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
@EnableConfigurationProperties(value = TigerRpcClientProperties.class)
public class TigerRpcClientAutoConfiguration {


    @Bean
    public TigerRpcClientProperties rpcClientProperties(Environment environment) {

        BindResult<TigerRpcClientProperties> result = Binder
                .get(environment).bind("tiger.rpc.client", TigerRpcClientProperties.class);
        TigerRpcClientProperties properties = result.get();

        Optional.ofNullable(properties.getTimeout()).ifPresent(timeout -> TigerConfigs.putValue(TigerConfigConstant.TIMEOUT_KEY, timeout));
        Optional.ofNullable(properties.getDiscoveryAddr()).
                ifPresent(discoverAddr -> TigerConfigs.putValue(TigerConfigConstant.CLIENT_DISCOVERY_ADDR_KEY, discoverAddr));
        Optional.ofNullable(properties.getRegistry()).
                ifPresent(registry -> TigerConfigs.putValue(TigerConfigConstant.CLIENT_DISCOVERY_ADDR_KEY, registry));
        Optional.ofNullable(properties.getRegistry()).
                ifPresent(registry -> TigerConfigs.putValue(TigerConfigConstant.REGISTRY_KEY, registry));
        Optional.ofNullable(properties.getRouteStrategy()).
                ifPresent(route -> TigerConfigs.putValue(TigerConfigConstant.ROUTE_STRATEGY_KEY, route));

        return properties;
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn(value = {"rpcClientProperties"})
    public IRoute route() {
        String routeStrategy = TigerConfigs
                .getDefaultString(TigerConfigConstant.ROUTE_STRATEGY_KEY);
        TigerSpiClass<? extends IRoute> spiClass = TigerSpiClassLoaderFactory
                .getSpiLoader(IRoute.class).getSpiClass(routeStrategy);
        return spiClass.getInstance();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn(value = {"rpcClientProperties"})
    public DiscoveryService discoveryService(@Autowired IRoute route) {
        String registry = TigerConfigs.getDefaultString(TigerConfigConstant.REGISTRY_KEY);
        String clientAddr = TigerConfigs
                .getDefaultString(TigerConfigConstant.CLIENT_DISCOVERY_ADDR_KEY);

        TigerSpiClass<? extends DiscoveryService> spiClass = TigerSpiClassLoaderFactory
                .getSpiLoader(DiscoveryService.class).getSpiClass(registry);

        return spiClass.getInstance(new Class[]{String.class, IRoute.class},
                new Object[]{clientAddr, route});
    }

    @Bean
    @ConditionalOnMissingBean
    public TigerRpcClientTransport clientTransport() {
       return new NettyRpcClientTransport();
    }

    @Bean
    public ClientStubProxyFactory proxyFactory() {
        return new ClientStubProxyFactory();
    }

    @Bean
    @DependsOn(value = {"discoveryService", "rpcClientProperties"})
    public TigerRpcClientProcessor clientProcessor(
            @Autowired TigerRpcClientTransport clientTransport,
            @Autowired DiscoveryService discoveryService,
            @Autowired ClientStubProxyFactory proxyFactory,
            @Autowired TigerRpcClientProperties rpcClientProperties) {

        return new TigerRpcClientProcessor(clientTransport, discoveryService, proxyFactory, rpcClientProperties);
    }




}
