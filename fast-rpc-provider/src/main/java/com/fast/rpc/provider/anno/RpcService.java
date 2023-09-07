package com.fast.rpc.provider.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于暴露服务接口
 */
@Target(ElementType.TYPE)   // 用于类上
@Retention(RetentionPolicy.RUNTIME)     // 在运行时可以获取
public @interface RpcService {

}
