package com.shawnliang.tiger.core;

import com.alibaba.fastjson.JSON;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
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
    }

    /**
     * 加载spi相关目录
     * @param path
     */
    private static void loadSpiCustom(String path) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> urls  = classLoader != null ? classLoader.getResources(path)
                : ClassLoader.getSystemClassLoader().getResources(path);

        if (urls != null) {
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                InputStreamReader input = null;
                BufferedReader reader = null;
                try {
                    input = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
                    reader = new BufferedReader(input);

                    StringBuilder context = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        context.append(line).append("\n");
                    }
                    Map map = JSON.parseObject(context.toString(), Map.class);
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

    }

}
