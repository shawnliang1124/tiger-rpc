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
public class TigerRpcResponse implements Serializable {

    private Header header;

    private Object data;

    private Boolean success;

    private String errorDetail;

}
