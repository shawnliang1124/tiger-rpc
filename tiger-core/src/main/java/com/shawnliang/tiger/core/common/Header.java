package com.shawnliang.tiger.core.common;

import java.util.UUID;
import lombok.Data;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Data
public class Header {

    /**
     * 请求Id
     */
    private String requestId;

    /**
     * 请求/响应 时间
     */
    private Long time;

    public static Header buildDefaultHeader() {
        Header header = new Header();

        header.setRequestId(UUID.randomUUID().toString().replace("-", ""));
        header.setTime(System.currentTimeMillis());
        return header;
    }
}
