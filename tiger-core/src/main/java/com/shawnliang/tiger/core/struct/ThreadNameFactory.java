package com.shawnliang.tiger.core.struct;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/5/16
 */
public class ThreadNameFactory implements ThreadFactory {

    /**
     * 系统全局线程池计数器
     */
    private static final AtomicInteger POOL_COUNT  = new AtomicInteger();

    /**
     * 当前线程池计数器
     */
    final AtomicInteger                threadCount = new AtomicInteger(1);
    /**
     * 线程组
     */
    private final ThreadGroup          group;
    /**
     * 线程名前缀
     */
    private final String               namePrefix;
    /**
     * 是否守护线程，true的话随主线程退出而退出，false的话则要主动退出
     */
    private final boolean              isDaemon;
    /**
     * 线程名第一前缀
     */
    private final String               firstPrefix = "TIGERPC-";


    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadCount.getAndIncrement(), 0);
        t.setDaemon(isDaemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    /**
     * 默认是非守护线程
     * @param secondPrefix
     */
    public ThreadNameFactory(String secondPrefix) {
        this(secondPrefix, false);
    }

    /**
     * 构造函数
     *
     * @param secondPrefix 第二前缀，前面会自动加上第一前缀，后面会自动加上-T-
     * @param daemon 是否守护线程，true的话随主线程退出而退出，false的话则要主动退出
     */
    public ThreadNameFactory(String secondPrefix, boolean daemon) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = firstPrefix + secondPrefix + "-" + POOL_COUNT.getAndIncrement() + "-T";
        isDaemon = daemon;
    }


}
