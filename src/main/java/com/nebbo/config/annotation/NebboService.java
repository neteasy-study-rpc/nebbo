package com.nebbo.config.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service //自定义注解，要让spring加载到，需要再加上@Service
public @interface NebboService {
    /**
     * 如果有多个接口，需要自己指定一个
     */
    Class<?> interfaceClass() default void.class;
}
