package com.fast.rpc.provider.handler;

import com.alibaba.fastjson.JSON;
import com.fast.rpc.common.RpcRequest;
import com.fast.rpc.common.RpcResponse;
import com.fast.rpc.provider.anno.RpcService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 自定义服务端业务处理类
 * 1、将标有 @RpcService注解的 Bean进行缓存
 * 2、接收客户端请求
 * 3、根据传递过来的 beanName 从缓存中查找到对应的 Bean
 * 4、解析请求中的方法名称、参数类型、参数信息
 * 5、反射调用 bean的方法
 * 6、给客户端进行响应
 */
@Component
@ChannelHandler.Sharable    // 设置通道共享
public class NettyServerHandler extends SimpleChannelInboundHandler<String> implements ApplicationContextAware {

   private static Map<String, Object> SERVICE_INSTANCE_MAP = new HashMap<>();

    /**
     * 1.将标有 @RpcService的注解的bean进行缓存
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 1.1 通过注解获取bean的集合
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        // 1.2 循环遍历
        Set<Map.Entry<String, Object>> entries = serviceMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            Object serviceBean = entry.getValue();
            if (serviceBean.getClass().getInterfaces().length == 0) {
                throw new RuntimeException("对外暴露的服务必须实现接口");
            }
            // 默认处理第一个作为缓存bean的名字
            String serviceName = serviceBean.getClass().getInterfaces()[0].getName();
            SERVICE_INSTANCE_MAP.put(serviceName, serviceBean);
            System.out.println(SERVICE_INSTANCE_MAP);
        }
    }

    /**
     * 2.通道读取就绪事件--读取客户端的消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 2.接受客户端的请求
        RpcRequest rpcRequest = JSON.parseObject(msg, RpcRequest.class);
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        // 业务处理
        try {
            rpcResponse.setResult(handler(rpcRequest));
        } catch (Exception e) {
            rpcResponse.setError(e.getMessage());
        }
        // 5. 给客户端响应
        ctx.writeAndFlush(JSON.toJSONString(rpcResponse));
    }

    private Object handler(RpcRequest rpcRequest) throws InvocationTargetException {
        // 3.根据传递过来的beanName从缓存中查找
        Object serviceBean = SERVICE_INSTANCE_MAP.get(rpcRequest.getClassName());
        if (serviceBean == null) {
            throw new RuntimeException("服务端每页找到服务");
        }
        // 4.通过反射调用bean的方法
        // 创建代理对象
        FastClass proxyClass = FastClass.create(serviceBean.getClass());
        FastMethod method = proxyClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        return method.invoke(serviceBean, rpcRequest.getParameters());
    }
}
