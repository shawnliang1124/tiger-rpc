package com.shawnliang.tiger.core.cache;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
public class ReflectCache {

    static final ConcurrentMap<Class<?>, String> TYPE_STR_CACHE =
            new ConcurrentHashMap<>();

    static final ConcurrentMap<String, WeakHashMap<ClassLoader, Class<?>>> CLASS_CACHE =
            new ConcurrentHashMap<>();



    /**
     * 得到类描述缓存
     *
     * @param clazz 类
     * @return 类描述
     */
    public static String getTypeStrCache(Class<?> clazz) {
        return TYPE_STR_CACHE.get(clazz);
    }

    /**
     * 得到Class缓存
     *
     * @param typeStr 对象描述
     * @return 类
     */
    public static Class<?> getClassCache(String typeStr) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            return null;
        } else {
            Map<ClassLoader, Class<?>> temp = CLASS_CACHE.get(typeStr);
            return temp == null ? null : temp.get(classLoader);
        }
    }

    /**
     * 放入类描述缓存
     *
     * @param clazz   类
     * @param typeStr 对象描述
     */
    public static void putTypeStrCache(Class<?> clazz, String typeStr) {
        TYPE_STR_CACHE.put(clazz, typeStr);
    }

    public static void putClassCache(String typeStr, Class<?> clazz) {
        CLASS_CACHE.putIfAbsent(typeStr, new WeakHashMap<>());
        CLASS_CACHE.get(typeStr).put(clazz.getClassLoader(), clazz);
    }
}
