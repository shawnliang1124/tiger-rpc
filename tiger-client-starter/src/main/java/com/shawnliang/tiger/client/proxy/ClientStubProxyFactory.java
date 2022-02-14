package com.shawnliang.tiger.client.proxy;

import com.shawnliang.tiger.client.config.TigerRpcClientProperties;
import com.shawnliang.tiger.client.handler.TigerRpcInvocationHandler;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.core.discovery.DiscoveryService;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class ClientStubProxyFactory {

    private Map<Class<?>, Object> objectCache = new HashMap<>();

    public <T> T getProxy(Class<T> clazz, String version, TigerRpcClientTransport clientTransport,
            DiscoveryService discoveryService, TigerRpcClientProperties properties) {
        return (T) objectCache.computeIfAbsent(clazz, clz ->
                Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clz},
                        new TigerRpcInvocationHandler(clientTransport, discoveryService, properties, clazz, version)));
    }

}
