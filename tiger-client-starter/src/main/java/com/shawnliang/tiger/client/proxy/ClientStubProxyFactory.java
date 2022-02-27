package com.shawnliang.tiger.client.proxy;

import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.client.transport.TransMetaInfo;
import com.shawnliang.tiger.core.TigerConfigConstant;
import com.shawnliang.tiger.core.TigerConfigs;
import com.shawnliang.tiger.core.exception.RpcException;
import com.shawnliang.tiger.core.proxy.TigerProxy;
import com.shawnliang.tiger.core.spi.TigerSpiClass;
import com.shawnliang.tiger.core.spi.TigerSpiClassLoaderFactory;
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

    public <T> T getProxy(
            Class<T> interfaceClass,
            TransMetaInfo transMetaInfo,
            TigerRpcClientTransport clientTransport) {
        if (objectCache.get(interfaceClass) != null) {
            return (T) objectCache.get(interfaceClass);
        }

        // 使用SPI 工厂创建
        TigerSpiClass<? extends TigerProxy> proxySpiClass = TigerSpiClassLoaderFactory
                .getSpiLoader(TigerProxy.class)
                .getSpiClass(TigerConfigs.getDefaultString(TigerConfigConstant.PROXY_KEY));

        if (proxySpiClass == null) {
            throw new RpcException("real proxy class not found, proxy alias: " + TigerConfigs.getDefaultString(TigerConfigConstant.PROXY_KEY));
        }

        // TODO 是用JDK去创建，还是Javassist去创建
        TigerProxy tigerProxy = proxySpiClass
                .getInstance(new Class[]{TransMetaInfo.class, TigerRpcClientTransport.class},
                        new Object[]{transMetaInfo, clientTransport});
        T proxy = tigerProxy.getProxy(interfaceClass);

        // 缓存类中不存在代理对象，通过JDK序列化去创建
        objectCache.put(interfaceClass, proxy);

        return proxy;
    }

}
