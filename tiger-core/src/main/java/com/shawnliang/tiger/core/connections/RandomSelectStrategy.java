package com.shawnliang.tiger.core.connections;

import com.shawnliang.tiger.core.common.NettyConnection;
import com.shawnliang.tiger.core.common.TigerRpcConstant;
import com.shawnliang.tiger.core.exception.RpcException;
import com.shawnliang.tiger.core.spi.TigerSpiImpl;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/6/12
 */
@TigerSpiImpl(value = "random")
public class RandomSelectStrategy implements ConnectSelectStrategy {

    private boolean statusCheck = false;

    private static final int MAX_TIMES = 5;

    private final Random random  = new Random();

    public RandomSelectStrategy() {

    }

    public RandomSelectStrategy(boolean statusCheck) {
        this.statusCheck = statusCheck;
    }


    @Override
    public NettyConnection select(List<NettyConnection> connections) {
        if (connections == null || connections.size() == 0) {
            return null;
        }

        NettyConnection selectConnect = null;
        if (statusCheck) {
            List<NettyConnection> avaliableConns;
            avaliableConns = connections.stream().filter(conn ->
                    StringUtils.equals((CharSequence) conn.getAttributes(TigerRpcConstant.CONN_STATUS), TigerRpcConstant.CONN_STATUS_ON))
                    .collect(Collectors.toList());

            if (avaliableConns.size() == 0) {
                throw new RpcException("no avaliable conns after filter!");
            }

            selectConnect = randomSelect(avaliableConns);
        } else {
            selectConnect = randomSelect(connections);

        }

        return selectConnect;
    }

    private NettyConnection randomSelect(List<NettyConnection> connections) {
        if (connections == null || connections.size() == 0) {
            return null;
        }

        int retry = 0;
        NettyConnection choose = null;

        while (choose == null ||!choose.isFine() || retry++ < MAX_TIMES) {
            int index = this.random.nextInt(connections.size());
            choose = connections.get(index);
        }


        return choose;
    }
}
