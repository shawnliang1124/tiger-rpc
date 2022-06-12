package com.shawnliang.tiger.core.connections;

import com.shawnliang.tiger.core.common.NettyConnection;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Description :   连接池管理.
 *
 * @author : Phoebe
 * @date : Created in 2022/6/12
 */
public class ConnectionPool {

    private CopyOnWriteArrayList<NettyConnection> conns = new CopyOnWriteArrayList<>();

    private ConnectSelectStrategy selectStrategy;

    public ConnectionPool(ConnectSelectStrategy selectStrategy) {
        this.selectStrategy = selectStrategy;
    }

    /**
     * whether connection pool done init?
     */
    private volatile static boolean asyncDone = true;

    public int poolSize() {
        return conns.size();
    }

    public void addConn(NettyConnection connection) {
        if (connection == null) {
            return;
        }

        boolean res = conns.addIfAbsent(connection);
        if (res) {
            connection.increaseRefCount();
        }
    }

    public NettyConnection getConn() {
        return selectStrategy.select(new ArrayList<>(conns));
    }

    /**
     * 关闭连接
     * @param connection
     */
    public void removeAndTryClose(NettyConnection connection) {
        if (connection == null) {
            return;
        }

        boolean remove = this.conns.remove(connection);
        if (remove) {
            connection.decreaseRef();
        }

        if (connection.noRef()) {
            connection.close();
        }

    }

    public boolean isEmpty() {
        return conns.isEmpty();
    }

    public void removeAllAndClose() {
        for (NettyConnection conn : this.conns) {
            removeAndTryClose(conn);
        }

        this.conns.clear();
    }
}
