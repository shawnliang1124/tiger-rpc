package com.shawnliang.tiger.core.spi;

import com.shawnliang.tiger.core.common.TigerRpcConstant;
import com.shawnliang.tiger.core.exception.RpcException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :   可拓展接口的Loader对象.
 *
 * @author : Phoebe
 * @date : Created in 2022/2/20
 */
public class TigerSpiLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(TigerSpiLoader.class);

    /**
     * 可拓展的接口
     */
    private Class<T> interfaceClass;

    /**
     * 可拓展接口的全称
     */
    private String interfaceName;

    /**
     * 可拓展接口的专属注解
     */
    private TigerSpi tigerSpi;


    /**
     * 缓存 别名和实现类的MAP
     */
    private Map<String, TigerSpiClass<? extends T>> spiClassMap;

    /**
     * 通过别名获取 spi的包装实现类
     * @param aliasName 别名
     * @return 对应spi的包装实现类
     */
    public TigerSpiClass<? extends T> getSpiClass(String aliasName) {
        return spiClassMap.get(aliasName);
    }


    protected TigerSpiLoader(Class<T> interfaceClass) {
        if (interfaceClass == null ||
                !(interfaceClass.isInterface()) || !Modifier.isAbstract(interfaceClass.getModifiers())) {
            throw new RpcException("TigerSpiLoader's interfaceClass must be interface or abstract class!");
        }

        // 初始化loader的各个属性
        initProperty(interfaceClass);

        // 从指定目录进行文件加载
        loadSpiClassMapFromFile();
    }

    /**
     * 从指定目录进行文件加载
     */
    private synchronized void loadSpiClassMapFromFile() {
        // todo 现在只支持扫描特定目录，支持可配置化等待优化
        String[] spiBasePaths = TigerRpcConstant.SPI_BASE_PATHS;
        for (String spiBasePath : spiBasePaths) {
            String file = StringUtils.isBlank(tigerSpi.fileName()) ?
                    interfaceName : tigerSpi.fileName().trim();
            String totalPath = spiBasePath + file;
            BufferedReader reader = null;
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Enumeration<URL> urls  = classLoader != null ? classLoader.getResources(totalPath) :
                        ClassLoader.getSystemClassLoader().getResources(totalPath);

                if (urls != null) {
                    while (urls.hasMoreElements()) {
                        URL url = urls.nextElement();
                        reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                        String line;
                        while ((line = reader.readLine()) != null) {
                           readSpiLineContent(line);
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("load path error, path: {}", totalPath, e);
                throw new RpcException("load path error, path " + totalPath);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        logger.error("reader close error", e);
                    }
                }
            }

        }

    }

    private void initProperty(Class<T> interfaceClass) {
        // 初始化loader的属性
        this.interfaceClass = interfaceClass;
        this.interfaceName = interfaceClass.getName();
        TigerSpi tigerSpi = interfaceClass.getAnnotation(TigerSpi.class);
        if (tigerSpi == null) {
            throw new RpcException(interfaceName + "must be decorated by @TigerSpi");
        }
        this.tigerSpi = tigerSpi;
        this.spiClassMap = new ConcurrentHashMap<>();
    }


    /**
     * 读取spi配置文件的内容
     * @param content spi文件内容
     */
    private void readSpiLineContent(String content) {
        String[] keyValueFromContent = this.genSpiKeyValueFromContent(content);
        if (keyValueFromContent == null || keyValueFromContent.length != 2) {
            return;
        }

        // 获取alias和content
        String alias = keyValueFromContent[0].trim();
        String totalClass = keyValueFromContent[1];

        // 实例化class
        Class<?> tmp = null;
        try {
            tmp = Class.forName(totalClass, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            logger.error("class not found, so init class: {} failed", totalClass, e);
        }
        if (tmp == null) {
            return;
        }

        // 将实例化的class，装载到spiClassMap本地缓存中
        persistInSpiClassMap(alias, tmp);
    }

    /**
     * 将实例化的class，装载到spiClassMap本地缓存中
     * @param alias class的别名
     * @param loadClass class的实现类
     */
    private void persistInSpiClassMap(String alias, Class<?> loadClass) {
        if (!interfaceClass.isAssignableFrom(loadClass)) {
            throw new IllegalArgumentException("loadClass is not implemented from target interface,"
                    + "please check. loadClassName: " + loadClass.getName() + "interfaceName: " + interfaceName);
        }

        Class<? extends T> implClass = (Class<? extends T>) loadClass;
        TigerSpiImpl tigerSpiImpl = implClass.getAnnotation(TigerSpiImpl.class);
        if (tigerSpiImpl == null) {
            // 没有拓展点的注解，抛异常
            throw new IllegalArgumentException("can't be used in spi model, because loadClass" + loadClass.getName()
             + "don't own @TigerSpiImpl");
        }

        // 获取拓展点上的标识别名
        String value = tigerSpiImpl.value();
        if (!StringUtils.equals(value.trim(), alias.trim())) {
            throw new IllegalArgumentException("error in persisting map, because tigerSpiImpl's value is: "
            + value + ", but alias in file is: " + alias);
        }

        // 检查map中是否有同名
        TigerSpiClass<? extends T> old = spiClassMap.get(alias);
        if (old == null) {
            spiClassMap.put(alias, genTigerSpiClass(implClass, alias, tigerSpiImpl));
        } else {
            int oldOrder = old.getOrder();
            int newOrder = tigerSpiImpl.order();
            if (newOrder < oldOrder) {
                // 新的类顺序更小，越优先加载
                if (logger.isDebugEnabled()) {
                    logger.debug("newOrder is less than oldOrder, then replace spiClassMap's old value."
                            + " newOrder: {}, oldOrder: {}", newOrder, oldOrder);
                }
                spiClassMap.put(alias, genTigerSpiClass(implClass, alias, tigerSpiImpl));
            }
        }

    }

    private TigerSpiClass<? extends T> genTigerSpiClass(Class<? extends T> implClass, String aliasName, TigerSpiImpl tigerSpiImpl) {
        TigerSpiClass<? extends T> tigerSpiClass = new TigerSpiClass<>(implClass);
        tigerSpiClass.setSingleton(tigerSpiImpl.isSingleton());
        tigerSpiClass.setOrder(tigerSpiImpl.order());


        return tigerSpiClass;
    }

    /**
     * 将spi文件当行内容解析成数组形式，
     * @param content 单行文件内容
     * @return key是别名， value 是实现类全称
     * ex: zookeeper = com.shawnliang.tiger.core.register.ZkRegistryServiceImpl
     */
    private String[] genSpiKeyValueFromContent(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        content = StringUtils.trim(content);
        int index = StringUtils.indexOf(content, "#");
        if (content.length() == 0 || index == 0) {
            // 说明是注释，直接返回空
            return null;
        }

        int i = content.indexOf("=");
        if (i <= 0) {
            return null;
        }
        String alias = content.substring(0, i);
        String className = content.substring(i + 1).trim();
        return new String[] {alias, className};
    }

}
