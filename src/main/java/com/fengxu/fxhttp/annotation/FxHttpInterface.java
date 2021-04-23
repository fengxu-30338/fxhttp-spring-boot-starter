package com.fengxu.fxhttp.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * 标注一个接口使其能够被fxhttp代理
 */
public @interface FxHttpInterface  {
    // beanName
    String value() default "";
    // 基url
    String baseUrl();
    // 是否开启日志
    boolean startLog() default false;
}
