package com.nebbo.config.spring.context;

import com.nebbo.config.ProtocolConfig;
import com.nebbo.config.ReferenceConfig;
import com.nebbo.config.RegistryConfig;
import com.nebbo.config.ServiceConfig;
import com.nebbo.config.annotation.NebboReference;
import com.nebbo.config.annotation.NebboService;
import com.nebbo.config.util.NebboBootstrap;
import com.nebbo.rpc.protocol.limiter.RedisRateLimiterSetter;
import com.nebbo.rpc.protocol.limiter.annotation.RedisRateLimiter;
import com.nebbo.rpc.protocol.limiter.config.RedisRateLimiterConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
            startLimiterTimer(bean); // 方法上有@RedisRateLimiter,则启动令牌桶生成程序
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
                referenceConfig.setRetryTimes(nebboReference.retryTimes()); // 客户端重试次数

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

    /**
     * 方法上有@RedisRateLimiter,则启动令牌桶生成程序
     * @param bean
     */
    private void startLimiterTimer(Object bean){
        RedisRateLimiterSetter redisRateLimiterSetter;
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(RedisRateLimiter.class)) {
                RedisRateLimiter redisRateLimiter = method.getAnnotation(RedisRateLimiter.class);
                long permitsPerSecond = redisRateLimiter.permitsPerSecond();
                String key = redisRateLimiter.key();
                String host = redisRateLimiter.host();
                int port = redisRateLimiter.port();
                RedisRateLimiterConfig config = RedisRateLimiterConfig.builder()
                        .permitsPerSecond(permitsPerSecond)
                        .key(key).host(host).port(port)
                        .luaSetScript().luaTimerScript().build();
                redisRateLimiterSetter = new RedisRateLimiterSetter(config);
                // 生成令牌的速率，单位:个/毫秒
                long period = 1000L / redisRateLimiter.permitsPerSecond();
                if (redisRateLimiterSetter.isStartTimer()) {

                    ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);
                    threadPoolExecutor.scheduleAtFixedRate((Runnable) redisRateLimiterSetter, 100, period, TimeUnit.MILLISECONDS);
                    System.out.println("启动redis令牌桶定时程序");
                } else {
                    System.out.println("已经启动redis令牌桶定时程序");
                }
            }
        }
    }
}
