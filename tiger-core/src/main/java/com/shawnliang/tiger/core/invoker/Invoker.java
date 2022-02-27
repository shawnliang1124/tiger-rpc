package com.shawnliang.tiger.core.invoker;

import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.common.TigerRpcResponse;

/**
 * Description :  调用器 .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
public interface Invoker {

    /**
     * 执行真正的调用请求
     *
     * @param request 请求对象
     * @return
     */
    TigerRpcResponse doInvoke(TigerRpcRequest request);

}
