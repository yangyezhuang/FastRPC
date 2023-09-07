package com.fast.rpc.consumer.client;

import com.fast.rpc.consumer.handler.NettyRpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Netty客户端
 * 1.连接服务端
 * 2.关闭资源
 * 3.提供发送消息的方法
 * 4.
 * 5.
 */
@Component
public class NettyRpcClient implements InitializingBean, DisposableBean {
    @Autowired
    NettyRpcClientHandler nettyRpcClientHandler;

    // 创建线程池
    ExecutorService service = Executors.newCachedThreadPool();
    EventLoopGroup group = null;
    Channel channel = null;

    /**
     * 1.连接服务端
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 1.1 创建线程组
            group = new NioEventLoopGroup();
            // 1.2 创建客户端启动助手
            Bootstrap bootstrap = new Bootstrap();
            // 1.3 设置参数
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 添加编解码器
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new StringEncoder());
                            // 添加自定义处理类
                            socketChannel.pipeline().addLast(nettyRpcClientHandler);
                        }
                    });
            // 1.4 连接服务
            channel = bootstrap.connect("localhost", 8899).sync().channel();
        } catch (Exception e) {
            e.printStackTrace();
            // 关闭资源
            if (channel != null) {
                channel.close();
            }
            if (group != null) {
                group.shutdownGracefully();
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        // 关闭资源
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    /**
     * 3. 消息发送
     */
    public Object send(String msg) throws ExecutionException, InterruptedException {
        nettyRpcClientHandler.setReqMsg(msg);
        Future submit = service.submit(nettyRpcClientHandler);
        return submit.get();
    }
}
