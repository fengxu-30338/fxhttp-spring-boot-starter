package com.fengxu.fxhttp;

import com.fengxu.http.proxy.FxHttpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class FxBeanRegister {

    @Autowired
    private FxhttpConfigProperties httpProperties;

    @Autowired
    private DefaultListableBeanFactory ioc;

    /**
     * 生成代理对象，并注册到容器中
     */
    public void registerBean(){
        handlerBlocker();
        for (Map.Entry<String, FxhttpConfigProperties.HttpProxyProps> entry
                : httpProperties.getHttpProxy().entrySet()) {
            Object proxy = entry.getValue().generatorProxy();
            ioc.registerSingleton(entry.getKey(), proxy);
        }
    }


    /**
     * 处理被FxBlocker标注的拦截器类
     */
    private void handlerBlocker(){
        Map<String, Object> blockerMap = ioc.getBeansWithAnnotation(FxBlocker.class);
        for (Map.Entry<String, Object> blockerEntry : blockerMap.entrySet()) {
            Object blocker = blockerEntry.getValue();
            FxBlocker fxBlocker = blocker.getClass().getDeclaredAnnotation(FxBlocker.class);
            // 获取拦截器类中定义的所有方法
            Method[] blockerMethods = blocker.getClass().getDeclaredMethods();

            // 获取拦截器的目标代理beanName
            String targetBeanName = fxBlocker.target();

            // 处理method获取到所有被FxBlockerMethod标注的方法
            for (Method blockerMethod : blockerMethods) {
                blockerMethod.setAccessible(true);
                if(blockerMethod.isAnnotationPresent(FxBlockerMethod.class)){
                    FxBlockerMethod fxBlockerMethod = blockerMethod.getAnnotation(FxBlockerMethod.class);
                    // 如果方法上指定了fxhttp代理的bean名称就优先使用方法上的
                    targetBeanName = fxBlockerMethod.name().isEmpty() ? targetBeanName: fxBlockerMethod.name();
                    if(targetBeanName.isEmpty()){
                        // 类上和方法上的注解都没有配置目标代理的beanName,抛出异常
                        throw new RuntimeException("没有配置该拦截器作用的目标代理!");
                    }
                    if(httpProperties.getHttpProxy().containsKey(targetBeanName) == false){
                        throw new RuntimeException("未找到名为:"+targetBeanName+"的目标代理!");
                    }
                    // 注册拦截器
                    registerInterceptor(targetBeanName, fxBlockerMethod.value(), blocker, blockerMethod);
                }
            }
        }
    }


    /**
     * 注册拦截器
     */
    private void registerInterceptor(String targetBean, String[] patterns,Object blocker, Method method){
        // 获取指定代理的属性
        FxhttpConfigProperties.HttpProxyProps httpProxyProps = httpProperties.getHttpProxy().get(targetBean);
        List<Pattern> patternList = new ArrayList<>();
        for (String pattern : patterns) {
            patternList.add(Pattern.compile(pattern));
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        int interceptorIndex = -1;
        try {
            for (int i = 0; i < parameterTypes.length; i++) {
                if(FxHttpInterceptor.class.isAssignableFrom(parameterTypes[i])){
                    interceptorIndex = i;
                } else {
                    args[i] = ioc.getBean(parameterTypes[i]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("未找到该方法的参数类型: " + e.getMessage());
        }
        if(interceptorIndex == -1){
            throw new RuntimeException("未找到FxHttpInterceptor拦截器类型参数");
        }
        int finalInterceptorIndex = interceptorIndex;
        httpProxyProps.getPatternMap().put(patternList, interceptor -> {
            args[finalInterceptorIndex] = interceptor;
            try {
                method.invoke(blocker,args);
            } catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        });
    }

}
