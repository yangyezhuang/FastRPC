package com.fast.rpc.provider.server;

import com.fast.rpc.provider.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Netty的服务端
 * 启动服务端，监听端口
 */
@Component
public class NettyRpcServer implements DisposableBean {

    @Autowired
    private NettyServerHandler nettyServerHandler;
    EventLoopGroup bossGroup = null;
    EventLoopGroup workerGroup = null;

    public void start(String host, int port) {
        try {
            // 1.创建线程组
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            // 2.设置启动助手
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 3.设置启动参数
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 4.向pipeline中添加自定义业务处理handler
                            // 设置编解码器
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new StringEncoder());
                            // 添加自定义处理器
                            socketChannel.pipeline().addLast(nettyServerHandler);
                        }
                    });
            // 5.绑定ip和端口
            ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
            System.out.println("=====Netty服务端启动成功=====");
            // 监听通道的关闭状态
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 关闭资源
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }
    }

    /**
     * spring容器关闭就执行destroy()方法
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        // 关闭资源
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
