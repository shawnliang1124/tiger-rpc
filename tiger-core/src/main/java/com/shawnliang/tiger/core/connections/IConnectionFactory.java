package com.shawnliang.tiger.core.connections;

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
     * 初始化DefaultConnectionManager
     */
    void init(ConnectionEventHandler connectionEventHandler);


    /**
     * 根据Url 配置创建netty连接
     * @param urlInfo
     * @return
     */
    NettyConnection createNettyConnection(UrlInfo urlInfo) throws Exception;


    /**
     * create netty conn, if urlinfo is not exist in the connection pool
     * @param urlInfo
     * @return
     */
    NettyConnection getAndCreateIfNotExist(UrlInfo urlInfo);

    /**
     * 检查连接的合法性
     * @param connection
     */
    void check(NettyConnection connection);

}
