package com.nebbo.config.spring.annotation;

import com.nebbo.config.spring.context.NebboConfigurationRegistrar;
import com.nebbo.config.spring.context.NebboPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动 Nebbo功能
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({NebboPostProcessor.class, NebboConfigurationRegistrar.class})
public @interface EnableNebbo {
}
