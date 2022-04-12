package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.core.common.TigerRpcResponse;
import com.shawnliang.tiger.core.exception.RpcException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class TransportCache {

    private static final Map<String, TigerRpcResponseFuture<TigerRpcResponse>> cacheMap = new ConcurrentHashMap<>();

    private static ThreadLocal<TigerRpcResponseFuture<TigerRpcResponse>> threadLocals = new ThreadLocal<>();


    public static void add(String requestId, TigerRpcResponseFuture<TigerRpcResponse> responseFuture) {
        cacheMap.put(requestId, responseFuture);
        TigerRpcResponseFuture<TigerRpcResponse> value = threadLocals.get();
        if (value == null) {
            threadLocals.set(responseFuture);
        }

    }

    public static Object getRpcFuture(long timeout) {
        TigerRpcResponseFuture<TigerRpcResponse> future = threadLocals.get();
        if (future == null) {
            throw new RpcException("now thread" + Thread.currentThread().getName() + "doesn't contain future");
        }

        // 不为空
        Object rtn;
        try {
            TigerRpcResponse response = future.get(timeout, TimeUnit.MILLISECONDS);
            rtn = response.getData();
        } catch (Exception e) {
            log.error("get future error", e);
            throw new RpcException("get future error");
        }

        // 删除上下文
        threadLocals.remove();
        return rtn;
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
