package com.fast.rpc.consumer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * 客户端业务处理类
 */
@Component
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<String> implements Callable {
    ChannelHandlerContext context;

    private String reqMsg;  //发送消息
    private String respMsg;  //接受消息

    public void setReqMsg(String reqMsg) {
        this.reqMsg = reqMsg;
    }

    /**
     * 通道读取就绪事件--读取服务端消息
     *
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    protected synchronized void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        respMsg = msg;
        // 唤醒等待线程
        notify();
    }

    /**
     * 通道连接就绪事件
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }

    /**
     * 给服务端发送消息
     *
     * @return
     * @throws Exception
     */
    @Override
    public synchronized Object call() throws Exception {
        context.writeAndFlush(reqMsg);
        // 将线程处于等待状态
        wait();
        return respMsg;
    }


}
