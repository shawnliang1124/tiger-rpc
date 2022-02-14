package com.shawnliang.tiger.core.common;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class ResponseBuilder<T> {

    public static <T> TigerRpcResponse buildSuccess(T data) {
        TigerRpcResponse response = new TigerRpcResponse();

        response.setData(data);
        response.setSuccess(true);
        response.setErrorDetail(null);
        return response;
    }

    public static <T> TigerRpcResponse buildFailWithError(String errorDetail) {
        TigerRpcResponse response = new TigerRpcResponse();

        response.setSuccess(false);
        response.setErrorDetail(errorDetail);
        return response;
    }

}
