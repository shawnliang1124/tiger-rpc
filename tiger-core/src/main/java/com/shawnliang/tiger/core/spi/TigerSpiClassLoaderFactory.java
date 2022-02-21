package com.shawnliang.tiger.core.spi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description :spi实现的工厂类   .
 *              目的是根据interface，找到对应的classLoader
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
public class TigerSpiClassLoaderFactory {

    /**
     * 保存接口，和接口ClassLoader的map
     */
    private static final Map<Class<?>, TigerSpiLoader<?>> SPI_LOADER_MAP = new ConcurrentHashMap<>();

    private TigerSpiClassLoaderFactory() {

    }

    public static <T> TigerSpiLoader<T> getSpiLoader(Class<T> interfaceClass) {
        TigerSpiLoader<?> tigerSpiLoader = SPI_LOADER_MAP.get(interfaceClass);
        if (tigerSpiLoader == null) {
            synchronized (TigerSpiClassLoaderFactory.class) {
                // double check,防止别的线程已经初始化过，重新初始化
                tigerSpiLoader = SPI_LOADER_MAP.get(interfaceClass);
                if (tigerSpiLoader == null) {
                    tigerSpiLoader = new TigerSpiLoader<>(interfaceClass);
                    SPI_LOADER_MAP.put(interfaceClass, tigerSpiLoader);
                }
            }
        }

        return (TigerSpiLoader<T>) tigerSpiLoader;
    }

}
