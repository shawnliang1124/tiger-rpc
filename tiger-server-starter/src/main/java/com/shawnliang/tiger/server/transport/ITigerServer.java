package com.shawnliang.tiger.server.transport;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public interface ITigerServer {

    /**
     * 开启应用
     * @param port
     */
    void start(int port);

    /**
     * 停止应用
     */
    void stop();

}
