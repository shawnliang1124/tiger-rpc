package com.shawnliang.tiger.client.handler;

import com.shawnliang.tiger.client.config.TigerRpcClientProperties;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.client.transport.TransMetaInfo;
import com.shawnliang.tiger.core.common.Header;
import com.shawnliang.tiger.core.common.ServiceInfo;
import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.common.TigerRpcResponse;
import com.shawnliang.tiger.core.discovery.DiscoveryService;
import com.shawnliang.tiger.core.exception.RpcException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Description :   动态代理，去发送tcp请求.
 * 该类等同于方法增强，client只是简单的Java参数调用，但是实际上是由该类去真正执行tcp的请求发送
 * 对于调用端，可以屏蔽各种网络的细节
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class TigerRpcInvocationHandler implements InvocationHandler {

    private TigerRpcClientTransport clientTransport;

    private DiscoveryService discoveryService;

    private TigerRpcClientProperties properties;

    /**
     * 被代理的类
     */
    private Class<?> proxyClass;

    /**
     * 被代理类的版本
     */
    private String version;

    public TigerRpcInvocationHandler(
            TigerRpcClientTransport clientTransport,
            DiscoveryService discoveryService,
            TigerRpcClientProperties properties, Class<?> proxyClass, String version) {
        this.clientTransport = clientTransport;
        this.discoveryService = discoveryService;
        this.properties = properties;
        this.proxyClass = proxyClass;
        this.version = version;
    }

    /**
     * 拿到被代理的类，相关参数，发送tcp的请求
     * @param proxy
     * @param method 被代理的方法
     * @param args 被代理方法的参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String serviceName = StringUtils.join(proxyClass.getName(), "_", version);
        ServiceInfo serviceInfo = discoveryService.discovery(serviceName);
        if (serviceInfo == null) {
            throw new RpcException("service not found!");
        }

        //method、args等这些都是被代理方法的数据
        TigerRpcRequest tigerRpcRequest = new TigerRpcRequest();
        tigerRpcRequest.setHeader(Header.buildDefaultHeader());
        tigerRpcRequest.setMethod(method.getName());
        tigerRpcRequest.setParams(args);
        tigerRpcRequest.setParamsType(method.getParameterTypes());
        tigerRpcRequest.setServiceName(serviceName);

        // 构建参数向server, 发送rpc请求
        TransMetaInfo transMetaInfo = TransMetaInfo.builder()
                .address(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .timeout(properties.getTimeout())
                .request(tigerRpcRequest).build();
        TigerRpcResponse response = clientTransport.sendRequest(transMetaInfo);
        if (response == null) {
            log.error("rpc 请求超时");
            throw new RpcException("rpc调用结果失败，请求超时：timeout" + properties.getTimeout());
        }

        return response.getData();
    }
}
