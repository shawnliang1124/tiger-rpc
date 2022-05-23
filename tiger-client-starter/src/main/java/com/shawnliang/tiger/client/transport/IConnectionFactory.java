package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.core.common.NettyConnection;
import com.shawnliang.tiger.core.common.UrlInfo;

/**
 * Description :   连接工厂.
 *
 * @author : Phoebe
 * @date : Created in 2022/5/23
 */
public interface IConnectionFactory {


    /**
     * 初始化
     */
    void init(ConnectionEventHandler connectionEventHandler);

    /**
     *  创建netty连接
     *
     * @param ip
     * @param port
     * @param timeout
     * @return
     */
    NettyConnection createNettyConnection(String ip, int port, int timeout) throws Exception;

    /**
     * 创建netty url info
     * @param urlInfo
     * @return
     */
    NettyConnection createNettyConnection(UrlInfo urlInfo);

}
