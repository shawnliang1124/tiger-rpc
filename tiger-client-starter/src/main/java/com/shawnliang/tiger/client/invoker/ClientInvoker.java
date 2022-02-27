package com.shawnliang.tiger.client.invoker;

import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.common.TigerRpcResponse;
import com.shawnliang.tiger.core.invoker.Invoker;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
public class ClientInvoker implements Invoker {

    private TigerRpcClientTransport clientTransport;

    public ClientInvoker(TigerRpcClientTransport clientTransport) {
        this.clientTransport = clientTransport;
    }

    @Override
    public TigerRpcResponse doInvoke(TigerRpcRequest request) {
        // TODO
        return null;
    }
}
