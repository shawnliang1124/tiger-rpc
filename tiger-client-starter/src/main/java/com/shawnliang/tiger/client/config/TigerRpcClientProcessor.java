package com.shawnliang.tiger.client.config;

import com.shawnliang.tiger.client.annonations.TigerRpcReference;
import com.shawnliang.tiger.client.proxy.ClientStubProxyFactory;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.client.transport.TransMetaInfo;
import com.shawnliang.tiger.core.common.ServiceInfo;
import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.discovery.DiscoveryService;
import com.shawnliang.tiger.core.exception.RpcException;
import com.shawnliang.tiger.core.factory.TigerRpcRequestFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

                    // ???????????????????????????Client??? @TigerRpcReference ?????????????????????
                    if (tigerRpcReference != null) {
                        Object bean = applicationContext.getBean(clazz);
                        field.setAccessible(true);

                        // ?????????????????????????????????TigerRequest????????????
                        String serviceName = StringUtils.join(field.getType().getCanonicalName(), "_", tigerRpcReference.version());
                        TigerRpcRequest tigerRpcRequest = TigerRpcRequestFactory
                                .genDefaultRequest(serviceName);

                        // ??????tcp????????????????????????
                        TransMetaInfo transMetaInfo = buildTransMetaInfoWithoutRequest(serviceName, tigerRpcReference);
                        transMetaInfo.setRequest(tigerRpcRequest);

                        // ??????????????????
                        Object proxy = clientStubProxyFactory
                                .getProxy(field.getType(), transMetaInfo, clientTransport);

                        // ?????????????????????????????????@TigerRpcReference ????????????
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


    /**
     * ??????????????????
     * @return
     */
    private TransMetaInfo buildTransMetaInfoWithoutRequest(String serviceName, TigerRpcReference tigerRpcReference){

        ServiceInfo serviceInfo = null;
        try {
            serviceInfo = discoveryService.discovery(serviceName);
        } catch (Exception e) {
            log.error("discover service name exception", e);
            throw new RpcException("discover service name exception!");
        }
        if (serviceInfo == null) {
            throw new RpcException("service not found!");
        }

        return TransMetaInfo.builder()
                .address(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .timeout(properties.getTimeout())
                .invokeType(tigerRpcReference.invoke())
                .connectNum(tigerRpcReference.connectNum())
                .request(null)
                .build();

    }
}
