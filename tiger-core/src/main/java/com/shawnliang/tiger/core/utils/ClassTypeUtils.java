package com.shawnliang.tiger.core.utils;

import com.shawnliang.tiger.core.cache.ReflectCache;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
public class ClassTypeUtils {

    /**
     * Class[]转String[]
     *
     * @param typeStrs 对象描述[]
     * @return Class[]
     */
    public static Class[] getClasses(String[] typeStrs) throws RuntimeException {
        if (typeStrs == null || typeStrs.length == 0) {
            return new Class[0];
        } else {
            Class[] classes = new Class[typeStrs.length];
            for (int i = 0; i < typeStrs.length; i++) {
                classes[i] = getClass(typeStrs[i]);
            }
            return classes;
        }
    }

    /**
     * String转Class
     *
     * @param typeStr 对象描述
     * @return Class[]
     */
    public static Class<?> getClass(String typeStr) {
        Class<?> clazz = ReflectCache.getClassCache(typeStr);
        if (clazz == null) {
            if ("void".equals(typeStr)) {
                clazz = void.class;
            } else if ("boolean".equals(typeStr)) {
                clazz = boolean.class;
            } else if ("byte".equals(typeStr)) {
                clazz = byte.class;
            } else if ("char".equals(typeStr)) {
                clazz = char.class;
            } else if ("double".equals(typeStr)) {
                clazz = double.class;
            } else if ("float".equals(typeStr)) {
                clazz = float.class;
            } else if ("int".equals(typeStr)) {
                clazz = int.class;
            } else if ("long".equals(typeStr)) {
                clazz = long.class;
            } else if ("short".equals(typeStr)) {
                clazz = short.class;
            } else {
                String jvmName = canonicalNameToJvmName(typeStr);
                clazz = TigerClassUtils.forName(jvmName);
            }
            ReflectCache.putClassCache(typeStr, clazz);
        }
        return clazz;
    }



    /**
     * 通用描述转JVM描述
     *
     * @param canonicalName 例如 int[]
     * @return JVM描述 例如 [I;
     */
    public static String canonicalNameToJvmName(String canonicalName) {
        boolean isArray = canonicalName.endsWith("[]");
        if (isArray) {
            String t = ""; // 计数，看上几维数组
            while (isArray) {
                canonicalName = canonicalName.substring(0, canonicalName.length() - 2);
                t += "[";
                isArray = canonicalName.endsWith("[]");
            }
            if ("boolean".equals(canonicalName)) {
                canonicalName = t + "Z";
            } else if ("byte".equals(canonicalName)) {
                canonicalName = t + "B";
            } else if ("char".equals(canonicalName)) {
                canonicalName = t + "C";
            } else if ("double".equals(canonicalName)) {
                canonicalName = t + "D";
            } else if ("float".equals(canonicalName)) {
                canonicalName = t + "F";
            } else if ("int".equals(canonicalName)) {
                canonicalName = t + "I";
            } else if ("long".equals(canonicalName)) {
                canonicalName = t + "J";
            } else if ("short".equals(canonicalName)) {
                canonicalName = t + "S";
            } else {
                canonicalName = t + "L" + canonicalName + ";";
            }
        }
        return canonicalName;
    }
}
