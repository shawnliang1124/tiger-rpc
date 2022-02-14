package com.shawnliang.tiger.client.config;

import com.shawnliang.tiger.client.proxy.ClientStubProxyFactory;
import com.shawnliang.tiger.client.transport.NettyRpcClientTransport;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.core.discovery.DiscoveryService;
import com.shawnliang.tiger.core.discovery.ZkDiscoveryServiceImpl;
import com.shawnliang.tiger.core.route.IRoute;
import com.shawnliang.tiger.core.route.LoadBalanceRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        return result.get();
    }

    @Bean
    @ConditionalOnMissingBean
    public IRoute route() {
        return new LoadBalanceRoute();
    }

    @Bean
    @ConditionalOnMissingBean
    public DiscoveryService discoveryService(@Autowired IRoute route, @Autowired TigerRpcClientProperties rpcClientProperties) {
        return new ZkDiscoveryServiceImpl(rpcClientProperties.getDiscoveryAddr(), route);
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
    public TigerRpcClientProcessor clientProcessor(@Autowired TigerRpcClientTransport clientTransport,
            @Autowired DiscoveryService discoveryService, @Autowired ClientStubProxyFactory proxyFactory, @Autowired TigerRpcClientProperties rpcClientProperties) {

        return new TigerRpcClientProcessor(clientTransport, discoveryService, proxyFactory, rpcClientProperties);
    }




}
