package com.shawnliang.tiger.core.spi;

import com.shawnliang.tiger.core.utils.TigerClassUtils;

/**
 * Description :   拓展点实现类对象.
 * 懒加载的机制，只有调用该对象的
 * TigerSpiClass<T>#getInstance方法，才会初始化
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
public class TigerSpiClass<T> {

    /**
     * 拓展点实现类
     */
    private Class<T> clazz;

    /**
     * 是否是单例
     */
    private boolean singleton;

    /**
     * 排序
     */
    private int order;

    /**
     * 在单例的情况下，单例对象引用
     * 多例的情况下，此项为空
     */
    private volatile T instance;

    public TigerSpiClass(Class<T> clazz) {
        this.clazz = clazz;
        /**
         * 别名
         */
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * 获取实例对象
     * @return
     */
    public T getInstance() {
       return getInstance(null, null);
    }

    /**
     *  得到服务端实例对象，如果是单例则返回单例对象，如果不是则返回新创建的实例对象
     * @param argTypes 构造函数参数类型
     * @param args 构造函数参数值
     * @return 扩展点对象实例 ext instance
     */
    public T getInstance(Class[] argTypes, Object[] args) {
        if (clazz == null) {
            return null;
        }

        // 如果不是单例，返回新创建的对象
        if (!singleton) {
            return TigerClassUtils.newInstanceWithArgs(clazz, argTypes, args);
        }

        // 单例的情况下
        if (instance != null) {
            return instance;
        }

        synchronized (this) {
            if (instance == null) {
               instance = TigerClassUtils.newInstanceWithArgs(clazz, argTypes, args);
            }
        }

        return instance;
    }

}
