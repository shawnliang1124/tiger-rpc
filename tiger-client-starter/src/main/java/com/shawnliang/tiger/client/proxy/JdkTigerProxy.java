package com.shawnliang.tiger.client.proxy;

import com.shawnliang.tiger.client.handler.TigerRpcInvocationHandler;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.client.transport.TransMetaInfo;
import com.shawnliang.tiger.core.proxy.TigerProxy;
import com.shawnliang.tiger.core.spi.TigerSpiImpl;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
@TigerSpiImpl(value = "jdk")
public class JdkTigerProxy implements TigerProxy {

    private final TransMetaInfo transMetaInfo;

    private final TigerRpcClientTransport clientTransport;

    public JdkTigerProxy(TransMetaInfo transMetaInfo,
            TigerRpcClientTransport clientTransport) {
        this.transMetaInfo = transMetaInfo;
        this.clientTransport = clientTransport;
    }

    @Override
    public <T> T getProxy(Class<T> interfaceClass) {
        TigerRpcInvocationHandler invocationHandler = new TigerRpcInvocationHandler(transMetaInfo ,clientTransport);
        T proxy = (T) java.lang.reflect.Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass}, invocationHandler);

        return proxy;
    }
}
