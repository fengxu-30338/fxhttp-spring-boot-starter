package com.fengxu.fxhttp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FxBlockerMethod {

    // 拦截器正则
    String[] value();

    // 设置要拦截的fxhttp代理的beanName,可覆盖@FxBlocke的配置
    String name() default "";

}
