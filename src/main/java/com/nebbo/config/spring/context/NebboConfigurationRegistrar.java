package com.nebbo.config.spring.context;

import com.nebbo.config.ProtocolConfig;
import com.nebbo.config.RegistryConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Field;

/**
 * Project: xl-rpc-all
 * Package: com.xl.rpc.config.spring
 * FileName: nebboConfiguration
 * Author:   Administrator
 * Date:     2020/12/26 21:22
 */
// 如何把自己创建的对象 放到 spring --  BeanDefinition，
// 这里不能用AutoWired,因为这里还没有开始对象的管理，对象也没有创建
public class NebboConfigurationRegistrar implements ImportBeanDefinitionRegistrar {
    StandardEnvironment environment;

    public NebboConfigurationRegistrar(Environment environment) {
        this.environment = (StandardEnvironment) environment;
    }

    // 让spring启动的时候，装置 没有注解或是xml配置的类(Bean)
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
                                        BeanNameGenerator importBeanNameGenerator) {
        // 告诉spring 让它 完成配置对象加载
        BeanDefinitionBuilder beanDefinitionBuilder = null;
        String value, systemProperty, fieldPrefix;
        // 1.2 ProtocolConfig - 读取每个配置项nebbo.protocol.name，然后赋值给ProtocolConfig对象
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ProtocolConfig.class);
        for(Field field:ProtocolConfig.class.getDeclaredFields()){

            fieldPrefix = "nebbo.protocol."+ field.getName();
            systemProperty = System.getProperty(fieldPrefix);  // 命令行参数，优先级最高
            if(systemProperty!=null && !"".equals(systemProperty)){
                value = systemProperty;
            }else{
                value = environment.getProperty(fieldPrefix);// 从配置文件 找到 相匹配的值
            }

            // value为空就是用配置类中的默认值
            if(value!=null && !"".equals(value)){
                beanDefinitionBuilder.addPropertyValue(field.getName(), value);
            }
        }

        registry.registerBeanDefinition("protocolConfig", beanDefinitionBuilder.getBeanDefinition());

        // 1.2 RegistryConfig - 读取配置 赋值 nebbo.registry.name
        beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RegistryConfig.class);
        for (Field field : RegistryConfig.class.getDeclaredFields()) {
            fieldPrefix = "nebbo.registry."+ field.getName();
            systemProperty = System.getProperty(fieldPrefix);  // 命令行参数，优先级最高
            if(systemProperty!=null && !"".equals(systemProperty)){
                value = systemProperty;
            }else{
                value = environment.getProperty(fieldPrefix);// 从配置文件 找到 相匹配的值
            }

            // value为空就是用配置类中的默认值
            if(value!=null && !"".equals(value)){
                beanDefinitionBuilder.addPropertyValue(field.getName(), value);
            }
        }
        registry.registerBeanDefinition("registryConfig", beanDefinitionBuilder.getBeanDefinition());
    }
}
