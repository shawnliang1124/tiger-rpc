package com.shawnliang.tiger.core.connections;

import com.shawnliang.tiger.core.common.NettyConnection;
import com.shawnliang.tiger.core.spi.TigerSpi;
import java.util.List;

/**
 * Description :   连接池选择策略.
 *
 * @author : Phoebe
 * @date : Created in 2022/6/12
 */
@TigerSpi
public interface ConnectSelectStrategy {

    /**
     * use select agri to select a netty connection
     * @param connections connections
     * @return
     */
    NettyConnection select(List<NettyConnection> connections);

}
