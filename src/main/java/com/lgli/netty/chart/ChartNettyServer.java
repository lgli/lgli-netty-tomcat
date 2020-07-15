package com.lgli.netty.chart;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;


/**
 * ChartNettyServer
 * @author lgli
 */
public class ChartNettyServer {

    public static void main(String[] args){
        //配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try{
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //针对主线程的配置，分配最大线程数
                    .option(ChannelOption.SO_BACKLOG,128)
                    //针对子线程的配置 保持长连接
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new MyChannelInitial());
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(new InetSocketAddress("localhost", 8080)).sync();
            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //出现异常，则释放资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    static class MyChannelInitial extends ChannelInitializer<SocketChannel>{

        protected void initChannel(SocketChannel socketChannel) throws Exception {
            System.out.println("client connection ：" + socketChannel.remoteAddress());
            socketChannel.pipeline()
                    .addLast("frame",new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                    .addLast("decode",new StringDecoder())
                    .addLast("encode",new StringEncoder())
                    .addLast("handler",new MyHandler());
        }
    }


    static class MyHandler extends SimpleChannelInboundHandler<String>{

        /**
         * 保存所有的连接
         */
        private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        /**
         * 接收到数据，发送给其他服务端
         * @param channelHandlerContext 发送数据的通道
         * @param s 发送的数据
         * @throws Exception
         */
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
            Channel inputChannel = channelHandlerContext.channel();
            for(Channel channel : channels){
                if(channel == inputChannel){
                    continue;
                }
                channel.writeAndFlush(inputChannel.remoteAddress()+":"+s+"\n");
            }
        }
    

        /**
         * 有新的连接连接进来
         * @param ctx
         * @throws Exception
         */
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            Channel inChannel = ctx.channel();
            //通知其他服务器有新的上线了
            for(Channel channel : channels){
                channel.writeAndFlush("欢迎"+inChannel.remoteAddress()+"进入聊天室! \n");
            }
            inChannel.writeAndFlush("欢迎您进入聊天室！\n");
            channels.add(inChannel);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            //通知其他服务器，下线了
            Channel outChannel = ctx.channel();
            for(Channel channel : channels){
                if(outChannel == channel){
                    continue;
                }
                channel.writeAndFlush(outChannel.remoteAddress()+"下线了 \n");
            }
            channels.remove(outChannel);
        }
    }
}
