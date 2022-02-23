package com.shawnliang.tiger.core;

import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
@Slf4j
public class TigerConfigs {

    private final static ConcurrentMap<String, Object> TIGER_CONFIG_MAP
            = new ConcurrentHashMap<>();

    static {
        init();
    }

    /**
     * 初始化
     */
    private static void init() {
        try {
            String result = loadSpiCustom("rpc-config-default.json");
            log.info("result is : {} ", result);
            Map map = JSONObject.parseObject(result, Map.class);
            TIGER_CONFIG_MAP.putAll(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载spi相关目录
     * @param path
     */
    private static String loadSpiCustom(String path) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> urls  = classLoader != null ? classLoader.getResources(path)
                : ClassLoader.getSystemClassLoader().getResources(path);
        StringBuilder context = new StringBuilder();

        if (urls != null) {
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                InputStreamReader input = null;
                BufferedReader reader = null;
                try {
                    input = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
                    reader = new BufferedReader(input);


                    String line;
                    while ((line = reader.readLine()) != null) {
                        context.append(line).append("\n");
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                }

            }
        }

        return context.toString();
    }

    public static String getDefaultString(String key) {
        if (!StringUtils.isBlank(key)) {
           return TIGER_CONFIG_MAP.get(key) == null ? null : (String) TIGER_CONFIG_MAP.get(key);
        }

        return null;
    }

    public static void putValue(String key, Object newValue) {
        TIGER_CONFIG_MAP.put(key, newValue);
    }

    public static Boolean getDefaultBoolean(String key) {
        if (!StringUtils.isBlank(key)) {
            return TIGER_CONFIG_MAP.get(key) == null ? null : (Boolean) TIGER_CONFIG_MAP.get(key);
        }

        return null;
    }

    public static Integer getDefaultInteger(String key) {
        if (!StringUtils.isBlank(key)) {
            return TIGER_CONFIG_MAP.get(key) == null ? null : (Integer) TIGER_CONFIG_MAP.get(key);
        }

        return null;
    }

    public static Long getDefaultLong(String key) {
        if (!StringUtils.isBlank(key)) {
            return TIGER_CONFIG_MAP.get(key) == null ? null : (Long) TIGER_CONFIG_MAP.get(key);
        }

        return null;
    }


}
