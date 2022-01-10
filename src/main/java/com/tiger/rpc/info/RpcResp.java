package com.tiger.rpc.info;

import lombok.Data;

/**
 * Description :  响应体 .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/9
 */
@Data
public class RpcResp {

    private Object data;

    private long time;

}
