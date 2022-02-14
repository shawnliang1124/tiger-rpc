package com.shawnliang.tiger.server.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public final class LocalCacheManager {

    private static final Map<String, Object> MAP = new ConcurrentHashMap<>();

    public static void saveCache(String serverName, Object server) {
        MAP.merge(serverName, server, (Object old, Object newObj) -> newObj);
    }

    public static Object get(String serverName) {
        return MAP.get(serverName);
    }

}
