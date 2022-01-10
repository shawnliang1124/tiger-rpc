package com.tiger.rpc.server;

/**
 * Description :   server抽象层.
 *
 * @author : Phoebe
 * @date : Created in 2022/1/8
 */
public interface IServer {

    /**
     * 开启
     */
    void start();

    /**
     * 结束
     */
    void stop();

}
