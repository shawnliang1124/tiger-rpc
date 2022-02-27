package com.shawnliang.tiger.core.utils;

import com.shawnliang.tiger.core.exception.RpcException;
import java.lang.reflect.Method;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
public class ReflectUtils {

    /**
     * 加载Method方法
     *
     * @param clazzName  类名
     * @param methodName 方法名
     * @param argsType   参数列表
     * @return Method对象
     */
    public static Method getMethod(String clazzName, String methodName, String[] argsType) {
        Class<?> clazz = TigerClassUtils.forName(clazzName);
        Class<?>[] classes = ClassTypeUtils.getClasses(argsType);
        return getMethod(clazz, methodName, classes);
    }

    /**
     * 加载Method方法
     *
     * @param clazz      类
     * @param methodName 方法名
     * @param argsType   参数列表
     * @return Method对象
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... argsType) {
        try {
            return clazz.getMethod(methodName, argsType);
        } catch (NoSuchMethodException e) {
            throw new RpcException(e.getMessage(), e);
        }
    }

}
