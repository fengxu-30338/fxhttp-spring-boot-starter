package com.fengxu.fxhttp.v2;

import com.fengxu.fxhttp.FxBeanRegisterExecutor;
import com.fengxu.fxhttp.FxhttpConfigProperties;
import com.fengxu.fxhttp.annotation.FxHttpInterface;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.*;

public class FxBeanRegisterV2 implements BeanDefinitionRegistryPostProcessor,
        ApplicationContextAware, ResourceLoaderAware {

    private ApplicationContext ioc;

    // 存放以注解形式配置的fxhttp代理接口的映射信息
    private FxhttpConfigProperties fxhttpConfigProperties;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        FxBeanRegisterExecutor registerExecutor = new FxBeanRegisterExecutor(ioc, fxhttpConfigProperties);
        registerExecutor.handlerBlocker(false);
        registerExecutor.registerBean(registry);
    }

    /**
     * 扫描将要被FxHttp代理接口的基本包
     * @param basePackage
     */
    private void scanFxHttpInterface(String basePackage) throws Exception{
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                ClassUtils.convertClassNameToResourcePath(basePackage) + "/**/*.class";
        // 获取基本包下所有类
        Resource[] resources = resolver.getResources(pattern);
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();

        // beanName - 接口属性
        Map<String, FxhttpConfigProperties.HttpProxyProps> proxyPropsMap = new HashMap<>();

        // 解析资源，获取用户配置带@FxHttpInterface接口信息
        for (Resource resource : resources) {
            MetadataReader metadataReader = readerFactory.getMetadataReader(resource);
            String className = metadataReader.getClassMetadata().getClassName();
            Class<?> targetClass = Class.forName(className);
            // 只获取被@FxHttpInterface标注的类
            if(targetClass.isAnnotationPresent(FxHttpInterface.class)){
                if(targetClass.isInterface() == false){
                    throw new RuntimeException(targetClass.getName()+"不是接口类型！");
                }

                // 解析注解上的信息存入到实体类中保存
                FxHttpInterface fxHttpInterface = targetClass.getAnnotation(FxHttpInterface.class);
                FxhttpConfigProperties.HttpProxyProps props = new FxhttpConfigProperties.HttpProxyProps();
                props.setBaseUrl(fxHttpInterface.baseUrl());
                props.setStartLog(fxHttpInterface.startLog());
                props.setClassName(className);
                // 获取默认bean名称: fxHttpInterface.value else 首字母大写
                String defaultBeanName = fxHttpInterface.value().isEmpty() ? targetClass.getSimpleName().substring(0,1).toLowerCase() +
                        targetClass.getSimpleName().substring(1) : fxHttpInterface.value();
                proxyPropsMap.put(defaultBeanName,props);
            }
        }
        fxhttpConfigProperties.setHttpProxy(proxyPropsMap);
    }


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource("application.yml");
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(resource);
        yamlFactory.afterPropertiesSet();
        Properties properties = yamlFactory.getObject();
        String basePackage = (String) (properties.get("fxhttp.basePackage") == null?
                        properties.get("fxhttp.base-package") :
                        properties.get("fxhttp.basePackage"));
        if(basePackage != null){
            try {
                fxhttpConfigProperties = new FxhttpConfigProperties();
                fxhttpConfigProperties.setBasePackage(basePackage);
                scanFxHttpInterface(basePackage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ioc = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
