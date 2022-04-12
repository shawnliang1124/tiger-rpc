package com.shawnliang.tiger.client.transport;

import com.shawnliang.tiger.core.common.TigerRpcRequest;
import lombok.Builder;
import lombok.Data;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Data
@Builder
public class TransMetaInfo {

    /**
     * 请求的参数
     */
    private TigerRpcRequest request;

    /**
     * 请求地址
     */
    private String address;

    /**
     * 请求端口
     */
    private Integer port;

    /**
     * 超时时间
     */
    private Long timeout;

    /**
     * 调用方式
     */
//    private String callType;
}
