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

    /**
     * 异步发送信息
     * @param transMetaInfo
     */
    void sendRequestAsync(TransMetaInfo transMetaInfo) throws Exception;

    /**
     * 连接
     */
    void connect();

    /**
     * 销毁
     */
    void destroy();


    /**
     * 是否可用
     * @return
     */
    boolean isAvaliable();

    /**
     * 获取当前的调用连接
     * @return
     */
    int getCurrentCount();
}
