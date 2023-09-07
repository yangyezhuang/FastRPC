package com.fast.rpc.consumer.proxy;

import com.alibaba.fastjson.JSON;
import com.fast.rpc.common.RpcRequest;
import com.fast.rpc.common.RpcResponse;
import com.fast.rpc.consumer.client.NettyRpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端的代理
 */
@Component
public class RpcClientProxy {
    @Autowired
    NettyRpcClient nettyRpcClient;

    Map<Class, Object> SERVICE_PROXY = new HashMap<>(); // 缓存集合

    /**
     * 获取代理对象
     *
     * @return
     */
    public Object getProxy(Class serviceClass) {
        // 从缓存中查找
        Object proxy = SERVICE_PROXY.get(serviceClass);
        if (proxy == null) {
            // 创建代理对象
            proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{serviceClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            // 1.封装请求对象
                            RpcRequest rpcRequest = new RpcRequest();
                            rpcRequest.setRequestId(UUID.randomUUID().toString());
                            rpcRequest.setClassName(method.getDeclaringClass().getName());
                            rpcRequest.setMethodName(method.getName());
                            rpcRequest.setParameterTypes(method.getParameterTypes());
                            rpcRequest.setParameters(args);
                            try {
                                // 2.发送消息
                                Object msg = nettyRpcClient.send(JSON.toJSONString(rpcRequest));
                                // 3.将消息转化
                                RpcResponse rpcResponse = JSON.parseObject(msg.toString(), RpcResponse.class);
                                if (rpcResponse.getError() != null) {
                                    throw new RuntimeException(rpcResponse.getError());
                                }
                                if (rpcResponse.getResult() != null) {
                                    return JSON.parseObject(rpcResponse.getResult().toString(), method.getReturnType());
                                }
                                return null;
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw e;
                            }
                        }
                    });
            // 放入缓存
            SERVICE_PROXY.put(serviceClass, proxy);
            return proxy;
        } else {
            return proxy;
        }
    }
}
