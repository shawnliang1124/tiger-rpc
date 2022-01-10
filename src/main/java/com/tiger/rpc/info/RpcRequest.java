package com.tiger.rpc.info;

import lombok.Data;

/**
 * Description :  rpc请求类.
 *
 * @author : Phoebe
 * @date : Created in 2022/1/9
 */
@Data
public class RpcRequest {

    private String javaClazz;

    private String interfaceName;

    private String protocol;

    private Object reqData;

}
