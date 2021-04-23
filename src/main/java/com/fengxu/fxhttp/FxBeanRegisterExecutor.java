package com.fengxu.fxhttp;

import com.fengxu.fxhttp.annotation.FxBlocker;
import com.fengxu.fxhttp.annotation.FxBlockerMethod;
import com.fengxu.http.proxy.FxHttpInterceptor;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * FxHttpBean注册抽象类
 */
public class FxBeanRegisterExecutor {

    private ListableBeanFactory beanFactory;

    private FxhttpConfigProperties fxhttpConfigProperties;

    // 是否扫描过拦截器
    private static volatile boolean isScanBlocker = false;

    // 拦截器注册信息列表
    private static List<BlockerRegisterInfo> blockerRegisterInfoList;

    static {
        blockerRegisterInfoList = new LinkedList<>();
    }

    public FxBeanRegisterExecutor(ListableBeanFactory beanFactory, FxhttpConfigProperties fxhttpConfigProperties) {
        this.beanFactory = beanFactory;
        this.fxhttpConfigProperties = fxhttpConfigProperties;
    }

    /**
     * 注册beanDefine到容器中
     *
     * @Author 风珝
     */
    public void registerBean(BeanDefinitionRegistry registry) {
        for (Map.Entry<String, FxhttpConfigProperties.HttpProxyProps> entry
                : fxhttpConfigProperties.getHttpProxy().entrySet()) {
            String className = entry.getValue().getClassName();
            Class<?> targetClass;
            try {
                targetClass = Class.forName(className);
            } catch (Exception e) {
                throw new RuntimeException("未找到目标类");
            }
            if (targetClass.isInterface() == false) {
                throw new RuntimeException(targetClass.getName() + "不是接口类型！");
            }
            if(beanFactory.getBeansOfType(targetClass).size() != 0){
                throw new RuntimeException("请勿重复创建相同类型的fxhttp代理！");
            }
            BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(targetClass);
            GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanBuilder.getRawBeanDefinition();
            ConstructorArgumentValues constructorArgumentValues = beanDefinition.getConstructorArgumentValues();
            constructorArgumentValues.addIndexedArgumentValue(0, targetClass);
            constructorArgumentValues.addIndexedArgumentValue(1, entry.getValue());
            beanDefinition.setBeanClass(FxHttpProxyFactory.class);
            beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            registry.registerBeanDefinition(entry.getKey(), beanDefinition);
        }
    }

    /**
     * 扫描拦截器信息
     */
    private void scanBlocker(){
        // 如果已经扫描过拦截器了，就不需要在扫描了
        if(isScanBlocker){
            return;
        }

        // 从容器中获取所有blocker
        List<Object> blockerList = beanFactory.getBeansWithAnnotation(FxBlocker.class)
                .entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (Object blocker : blockerList) {
            // 获取拦截器类上标注的目标对象
            final String target = blocker.getClass().getDeclaredAnnotation(FxBlocker.class).target();
            for (Method method : blocker.getClass().getDeclaredMethods()) {
                method.setAccessible(true);
                // 只处理被@FxBlockerMethod标注的拦截器方法
                if(method.isAnnotationPresent(FxBlockerMethod.class)){
                    FxBlockerMethod fxBlockerMethod = method.getAnnotation(FxBlockerMethod.class);
                    // 优先使用方法上的指定的拦截目标
                    String targetName = fxBlockerMethod.name().isEmpty() ? target : fxBlockerMethod.name();
                    if (targetName.isEmpty()) {
                        // 类上和方法上的注解都没有配置目标代理的beanName,抛出异常
                        throw new RuntimeException("没有配置该拦截器作用的目标代理!");
                    }
                    BlockerRegisterInfo blockerRegisterInfo = new BlockerRegisterInfo();
                    blockerRegisterInfo.setBlocker(blocker);
                    blockerRegisterInfo.setTarget(targetName);
                    blockerRegisterInfo.setPatterns(fxBlockerMethod.value());
                    blockerRegisterInfo.setMethod(method);
                    blockerRegisterInfoList.add(blockerRegisterInfo);
                }
            }
        }
        // 标注已经扫描过拦截器了
        isScanBlocker = true;
    }


    /**
     * 处理被FxBlocker标注的拦截器类
     *
     * @param throwable 未匹配到拦截器时是否抛出异常
     */
    public void handlerBlocker(boolean throwable) {
        scanBlocker();
        for (int i = 0; i < blockerRegisterInfoList.size(); i++) {
            BlockerRegisterInfo blockerRegisterInfo = blockerRegisterInfoList.get(i);
            if(fxhttpConfigProperties.getHttpProxy().containsKey(blockerRegisterInfo.target) == false){
                if(throwable){
                    throw new RuntimeException("拦截器未找到目标为" + blockerRegisterInfo.target +"的fxhttp代理!");
                } else {
                    continue;
                }
            }
            blockerRegisterInfo.setRegister(true);

            // 注册拦截器
            registerInterceptor(blockerRegisterInfo.getTarget(),
                    blockerRegisterInfo.getPatterns(),
                    blockerRegisterInfo.getBlocker(),
                    blockerRegisterInfo.getMethod());

            blockerRegisterInfoList.remove(i);
            i-= 1;
        }
    }


    /**
     * 注册拦截器
     */
    private void registerInterceptor(String targetBean, String[] patterns,Object blocker, Method method){
        // 获取指定代理的属性
        FxhttpConfigProperties.HttpProxyProps httpProxyProps = fxhttpConfigProperties.getHttpProxy().get(targetBean);
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
                    args[i] = beanFactory.getBean(parameterTypes[i]);
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

    /**
     * 拦截器注册信息类
     */
    private static class BlockerRegisterInfo{

        // 拦截器类
        private Object blocker;

        // 该拦截器作用的目标对象beanName
        private String target;

        // 该拦截器匹配的正则
        private String[] patterns;

        // 拦截器方法
        private Method method;

        // 该拦截器是否已经被注册过
        private boolean isRegister = false;

        public Object getBlocker() {
            return blocker;
        }

        public void setBlocker(Object blocker) {
            this.blocker = blocker;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public boolean isRegister() {
            return isRegister;
        }

        public void setRegister(boolean register) {
            isRegister = register;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String[] getPatterns() {
            return patterns;
        }

        public void setPatterns(String[] patterns) {
            this.patterns = patterns;
        }
    }

}
