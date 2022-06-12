package com.shawnliang.tiger.core.enums;

import lombok.AllArgsConstructor;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/6/12
 */
@AllArgsConstructor
public enum ProtocolEnums {

    /**
     * 协议枚举
     */
    REST("REST"),
    PROTO_STUFF("protostuff"),
    CUSTOM("custom")
    ;


    String name;

}
