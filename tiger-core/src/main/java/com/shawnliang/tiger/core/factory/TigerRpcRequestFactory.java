package com.shawnliang.tiger.core.factory;

import com.shawnliang.tiger.core.common.Header;
import com.shawnliang.tiger.core.common.TigerRpcRequest;

/**
 * Description :  请求工厂.
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
public class TigerRpcRequestFactory {

    private TigerRpcRequestFactory() {

    }

    /**
     * 构建默认的请求参数
     * @param serviceName 应用名
     * @return
     */
    public static TigerRpcRequest genDefaultRequest(String serviceName) {
        TigerRpcRequest tigerRpcRequest = new TigerRpcRequest();
        tigerRpcRequest.setServiceName(serviceName);
        tigerRpcRequest.setHeader(Header.buildDefaultHeader());

        return tigerRpcRequest;
    }

}
