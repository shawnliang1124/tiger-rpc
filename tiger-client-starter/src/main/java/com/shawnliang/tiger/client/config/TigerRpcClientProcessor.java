package com.shawnliang.tiger.client.config;

import com.shawnliang.tiger.client.annonations.TigerRpcReference;
import com.shawnliang.tiger.client.proxy.ClientStubProxyFactory;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.core.discovery.DiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class TigerRpcClientProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private TigerRpcClientTransport clientTransport;

    private DiscoveryService discoveryService;

    private ClientStubProxyFactory clientStubProxyFactory;

    private TigerRpcClientProperties properties;


    private ApplicationContext applicationContext;

    public TigerRpcClientProcessor(
            TigerRpcClientTransport clientTransport,
            DiscoveryService discoveryService,
            ClientStubProxyFactory clientStubProxyFactory,
            TigerRpcClientProperties properties) {
        this.clientTransport = clientTransport;
        this.discoveryService = discoveryService;
        this.clientStubProxyFactory = clientStubProxyFactory;
        this.properties = properties;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils
                        .resolveClassName(beanClassName, this.getClass().getClassLoader());
                ReflectionUtils.doWithFields(clazz, field -> {
                    TigerRpcReference tigerRpcReference = AnnotationUtils
                            .getAnnotation(field, TigerRpcReference.class);
                    if (tigerRpcReference != null) {
                        Object bean = applicationContext.getBean(clazz);
                        field.setAccessible(true);
                        Object proxy = clientStubProxyFactory
                                .getProxy(field.getType(), tigerRpcReference.version(), clientTransport,
                                        discoveryService, properties);
                        // 设置代理类
                        ReflectionUtils.setField(field, bean, proxy);
                    }
                });
            }

        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
