package com.shawnliang.tiger.core.common;

import java.io.Serializable;
import lombok.Data;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Data
public class TigerRpcRequest implements Serializable {

    private Header header;

    /**
     * 请求的服务名 + 版本
     */
    private String serviceName;

    /**
     * 请求调用的方法
     */
    private String method;

    private Class<?>[] paramsType;

    private Object[] params;

}
