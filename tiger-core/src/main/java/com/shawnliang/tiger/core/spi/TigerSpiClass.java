package com.shawnliang.tiger.core.spi;

/**
 * Description :   拓展点实现类对象.
 *
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
public class TigerSpiClass<T> {

    /**
     * 拓展点实现类
     */
    private Class<T> tClass;

    /**
     * 别名
     */
    private String aliasName;

    /**
     * 是否是单例
     */
    private boolean singleton;

    /**
     * 在单例的情况下，单例对象引用
     * 多例的情况下，此项为空
     */
    private T instance;

    public T getInstance() {
        // TODO 返回该类的对象
        return null;
    }

}
