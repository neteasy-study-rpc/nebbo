package com.nebbo.config.spring.context;

import com.nebbo.config.ProtocolConfig;
import com.nebbo.config.ReferenceConfig;
import com.nebbo.config.RegistryConfig;
import com.nebbo.config.ServiceConfig;
import com.nebbo.config.annotation.NebboReference;
import com.nebbo.config.annotation.NebboService;
import com.nebbo.config.util.NebboBootstrap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;

/**
 * Project: xl-rpc-all
 * Package: com.nebbo.config.spring
 * FileName: TRPCPostProcessor
 * Author:   Administrator
 * Date:     2020/12/26 21:22
 */

/**
 * spring扫描 初始化对象之后， 我要找到里面 TRpcService, 由于我们自己的rpc框架中的类，不在服务提供者运行的spring中
 * 所以需要通过PostProcessor的方式，将rpc框架中的相关类交给spring加载
 */
public class NebboPostProcessor implements ApplicationContextAware, InstantiationAwareBeanPostProcessor {
    ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * bean初始化后 做其他拓展
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName){
        if(bean.getClass().isAnnotationPresent(NebboService.class)){
            // 服务端启动后，程序走这里
            System.out.println("找到了需要开放网络访问的service实现类。构建serviceConfig配置");
            ServiceConfig serviceConfig = new ServiceConfig();
            serviceConfig.addProtocolConfig(applicationContext.getBean(ProtocolConfig.class));
            serviceConfig.addRegistryConfig(applicationContext.getBean(RegistryConfig.class));
            serviceConfig.setReference(bean);

            NebboService nebboService = bean.getClass().getAnnotation(NebboService.class);
            if(void.class == nebboService.interfaceClass()){
                serviceConfig.setService(bean.getClass().getInterfaces()[0]);
            } else {
                serviceConfig.setService(nebboService.interfaceClass());
            }

            // boot
            NebboBootstrap.export(serviceConfig);


        }

        // 2. 服务引用- 注入，客户端启动后，程序走这里
        for (Field field : bean.getClass().getDeclaredFields()) {
            try {
                if (!field.isAnnotationPresent(NebboReference.class)) {
                    continue; // 不继续下面的代码，继续循环
                }
                // 引用相关 配置 保存在一个对象里边 // TODO 思考：如果一个引用需要在多个类被使用，使用缓存来解决
                ReferenceConfig referenceConfig = new ReferenceConfig();
                referenceConfig.addRegistryConfig(applicationContext.getBean(RegistryConfig.class));
                referenceConfig.addProtocolConfig(applicationContext.getBean(ProtocolConfig.class));
                referenceConfig.setService(field.getType());

                NebboReference nebboReference = field.getAnnotation(NebboReference.class);
                referenceConfig.setLoadbalance(nebboReference.loadbalance());

                Object referenceBean = NebboBootstrap.getReferenceBean(referenceConfig);
                // 把获取到的代理对象referenceBean，注入到有@TRpcReference注解的类的成员变量中
                // 就是注入到OrderServiceImpl类的SmsService smsService
                field.setAccessible(true);
                field.set(bean, referenceBean);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bean;
    }
}
