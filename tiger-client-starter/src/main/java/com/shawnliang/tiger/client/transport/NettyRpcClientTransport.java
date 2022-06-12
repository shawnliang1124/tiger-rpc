package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.client.handler.TigerRpcClientHandler;
import com.shawnliang.tiger.core.codec.TigerRpcClientDecoder;
import com.shawnliang.tiger.core.codec.TigerRpcClientEncoder;
import com.shawnliang.tiger.core.common.NettyConnection;
import com.shawnliang.tiger.core.common.TigerRpcConstant;
import com.shawnliang.tiger.core.common.TigerRpcResponse;
import com.shawnliang.tiger.core.common.UrlInfo;
import com.shawnliang.tiger.core.connections.ConnectSelectStrategy;
import com.shawnliang.tiger.core.connections.DefaultConnectionFactory;
import com.shawnliang.tiger.core.utils.RemotingUtil;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class NettyRpcClientTransport implements TigerRpcClientTransport{


    private static final AtomicLong requestCount = new AtomicLong(0);

    private DefaultConnectionFactory defaultConnectionFactory ;


    public NettyRpcClientTransport(ConnectSelectStrategy selectStrategy) {

        TigerRpcClientHandler connectionEventHandler = new TigerRpcClientHandler();
        defaultConnectionFactory = new DefaultConnectionFactory(null, connectionEventHandler,
                new TigerRpcClientEncoder(), new TigerRpcClientDecoder(), selectStrategy);
        defaultConnectionFactory.init(null);
    }



    @Override
    public TigerRpcResponse sendRequest(TransMetaInfo transMetaInfo) throws Exception {
        TigerRpcResponseFuture<TigerRpcResponse> future = doSendRequest(transMetaInfo);

        // future.get，阻塞调用线程，等待结果的返回
        if (transMetaInfo.getTimeout() == null) {
            return future.get(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } else {
            return future.get(transMetaInfo.getTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void sendRequestAsync(TransMetaInfo transMetaInfo) throws Exception {
        // 发送完请求直接结束，释放调用线程
        doSendRequest(transMetaInfo);
    }

    @Override
    public void connect() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isAvaliable() {
        return false;
    }

    @Override
    public int getCurrentCount() {
        return 0;
    }

    private TigerRpcResponseFuture<TigerRpcResponse> doSendRequest(TransMetaInfo transMetaInfo) {
        TigerRpcResponseFuture<TigerRpcResponse> future = null;
        String invokeType = transMetaInfo.getInvokeType();
        switch (invokeType) {
            case TigerRpcConstant
                    .ASYNC:
                doSendByNetty(transMetaInfo);
            break;

            case TigerRpcConstant.SYNC:
                // 写入，并且等待该请求
                future = new TigerRpcResponseFuture<>();
                TransportCache.add(transMetaInfo.getRequest().getHeader().getRequestId(), future);

                doSendByNetty(transMetaInfo);
            break;

            default:
                break;

        }

        requestCount.incrementAndGet();

        return future;
    }

    /**
     * 使用netty 的channel发送消息
     * @param transMetaInfo
     */
    private void doSendByNetty(TransMetaInfo transMetaInfo) {
        // todo 从连接池中获取连接
        UrlInfo urlInfo = buildByTransMetaInfo(transMetaInfo);
        NettyConnection connection = defaultConnectionFactory.getAndCreateIfNotExist(urlInfo);

        // do some checks
        defaultConnectionFactory.check(connection);

        try {
            connection.getChannel().writeAndFlush(transMetaInfo.getRequest()).addListener((future -> {
                if (!future.isSuccess()) {
                    log.error("send failed!, the address is: {}",
                            RemotingUtil.parseLocalAddress(connection.getChannel()), future.cause());
                } else {
                    log.info("send success, msg: {}", transMetaInfo.getRequest());
                }
            }));
        } catch (Exception e) {
            log.error("send request failed!, address: {}", RemotingUtil.parseLocalAddress(connection.getChannel()), e);
        }

    }

    /**
     * build  url info by trans meta
     * @param transMetaInfo
     * @return
     */
    private UrlInfo buildByTransMetaInfo(TransMetaInfo transMetaInfo) {
        UrlInfo urlInfo = new UrlInfo(transMetaInfo.getAddress(), transMetaInfo.getPort(), transMetaInfo.getProtocol());
        urlInfo.setConnectTimeout(transMetaInfo.getTimeout());
        urlInfo.setConnNum(transMetaInfo.getConnectNum());


        return urlInfo;
    }
}
