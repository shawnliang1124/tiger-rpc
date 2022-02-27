package com.shawnliang.tiger.core.proxy;

import com.shawnliang.tiger.core.spi.TigerSpi;

/**
 * Description :  代理类 .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
@TigerSpi
public interface TigerProxy {

    /**
     * 生成代理的对象
     * @param interfaceClass 接口类
     * @param <T>
     * @return
     */
    <T> T getProxy(Class<T> interfaceClass);

}
