package com.shawnliang.tiger.client.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :   .关闭连接工厂
 *
 * @author : Phoebe
 * @date : Created in 2022/5/18
 */
public class ClientTransFactory {

    private static final Logger logger = LoggerFactory.getLogger(ClientTransFactory.class);


    /**
     * 释放连接池
     * @param transport
     * @param disconnectTimeout
     */
    public static void releaseTrans(TigerRpcClientTransport transport, long disconnectTimeout) {
        if (transport == null) {
            return;
        }

        int currentCount = transport.getCurrentCount();
        if (currentCount > 0) {
            long start = System.currentTimeMillis();

            // 如果当前还有请求，就sleep一会
            while (System.currentTimeMillis() - start < disconnectTimeout
                    && transport.getCurrentCount() > 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            int nowCurrent = transport.getCurrentCount();
            if (nowCurrent > 0) {
                logger.warn("current request is : {}, but client need to close", nowCurrent);
            }

            // 进行client的关闭
            transport.destroy();
        }

    }

}
