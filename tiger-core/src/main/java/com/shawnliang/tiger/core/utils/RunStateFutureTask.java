package com.shawnliang.tiger.core.utils;

import com.shawnliang.tiger.core.exception.RpcException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description :  future task，用于将callable异步获取 .
 *
 * @author : Phoebe
 * @date : Created in 2022/6/12
 */
public class RunStateFutureTask<V> extends FutureTask<V> {

    public RunStateFutureTask(Callable<V> callable) {
        super(callable);
    }

    private final AtomicBoolean hasRun = new AtomicBoolean();

    @Override
    public void run() {
        hasRun.set(true);
        super.run();
    }

    public V getAfterRun() throws ExecutionException, InterruptedException {
        if (!hasRun.get()) {
            throw new RpcException("future task has not run!");
        }

        if (!isDone()) {
            throw new RpcException("future task is not done!");
        }

        return super.get();
    }


}
