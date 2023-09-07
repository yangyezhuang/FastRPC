package com.fast.rpc.consumer.processor;

import com.fast.rpc.consumer.anno.RpcReference;
import com.fast.rpc.consumer.proxy.RpcClientProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * bean的后置增强
 */
@Component
public class MyBeanProcessor implements BeanPostProcessor {
    @Autowired
    RpcClientProxy rpcClientProxy;

    /**
     * 自定义注解的注入
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 1.查看bean的字段中有没有对应注解
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            // 2.查找字段中是否包含注解
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if (annotation != null) {
                // 3.获取代理对象
                Object proxy = rpcClientProxy.getProxy(field.getType());
                try {
                    // 4.属性注入
                    field.setAccessible(true);
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
