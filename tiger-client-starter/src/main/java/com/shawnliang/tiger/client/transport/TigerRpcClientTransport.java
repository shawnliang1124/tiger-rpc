package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.core.common.TigerRpcResponse;

/**
 * Description :   发送网络请求.
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public interface TigerRpcClientTransport {

    /**
     * 发送数据
     * @param transMetaInfo 请求对象
     * @return
     */
    TigerRpcResponse sendRequest(TransMetaInfo transMetaInfo) throws Exception;

}
