package com.shawnliang.tiger.core.exception;

import com.shawnliang.tiger.core.common.BaseRpcErrorResp;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class RpcException extends RuntimeException {

    private static final long serialVersionUID = 3365624081242234230L;

    private Integer errorCode;

    private String errorMsg;


    public RpcException() {
        super();
    }

    public RpcException(String msg) {
        super(msg);
    }

    public RpcException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(BaseRpcErrorResp baseRpcErrorResp) {
        this.errorCode = baseRpcErrorResp.getErrorCode();
        this.errorMsg = baseRpcErrorResp.getErrorMsg();
    }
}
