package com.shawnliang.tiger.core.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@AllArgsConstructor
@Data
public class BaseRpcErrorResp {

    private Integer errorCode;

    private String errorMsg;

}
