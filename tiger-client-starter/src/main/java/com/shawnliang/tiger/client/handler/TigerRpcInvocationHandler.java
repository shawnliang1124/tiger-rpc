package com.shawnliang.tiger.client.handler;

import com.shawnliang.tiger.client.proxy.javaassist.UselessInvocationHandler;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.client.transport.TransMetaInfo;
import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.common.TigerRpcResponse;
import com.shawnliang.tiger.core.exception.RpcException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import lombok.extern.slf4j.Slf4j;

/**
 * Description :   动态代理，去发送tcp请求.
 * 该类等同于方法增强，client只是简单的Java参数调用，但是实际上是由该类去真正执行tcp的请求发送
 * 对于调用端，可以屏蔽各种网络的细节
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class TigerRpcInvocationHandler extends Proxy implements InvocationHandler {

    public TigerRpcInvocationHandler(TransMetaInfo transMetaInfo, TigerRpcClientTransport clientTransport) {
        super(new UselessInvocationHandler());
        this.transMetaInfo = transMetaInfo;
        this.clientTransport = clientTransport;

    }

    private final TransMetaInfo transMetaInfo;

    private final TigerRpcClientTransport clientTransport;


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

        //method、args等这些都是被代理方法的数据
        TigerRpcRequest tigerRpcRequest = this.transMetaInfo.getRequest();
        tigerRpcRequest.setMethod(method.getName());
        tigerRpcRequest.setParams(args);
        tigerRpcRequest.setParamsType(method.getParameterTypes());

        TigerRpcResponse response = clientTransport.sendRequest(transMetaInfo);
        if (response == null) {
            log.error("rpc 请求超时");
            throw new RpcException("rpc调用结果失败，请求超时：timeout" + transMetaInfo.getTimeout());
        }

        return response.getData();
    }
}
