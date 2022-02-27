package com.shawnliang.tiger.client.proxy.javaassist;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
public class UselessInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        throw new UnsupportedOperationException("This class just for adapted to java proxy");
    }
}
