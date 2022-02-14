package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.core.common.TigerRpcResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class TransportCache {

    private static final Map<String, TigerRpcResponseFuture<TigerRpcResponse>> cacheMap = new ConcurrentHashMap<>();

    public static void add(String requestId, TigerRpcResponseFuture<TigerRpcResponse> responseFuture) {
        cacheMap.put(requestId, responseFuture);
    }

    /**
     * 唤醒正在等待的 TigerRpcResponseFuture
     * @param requestId
     * @param rpcResponse
     */
    public static void fillResponse(String requestId, TigerRpcResponse rpcResponse) {
        TigerRpcResponseFuture<TigerRpcResponse> responseFuture = cacheMap
                .get(requestId);
        if (responseFuture != null) {
            responseFuture.setResponse(rpcResponse);
            cacheMap.remove(requestId);
        }
    }

}
