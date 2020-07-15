package com.lgli.netty.chart;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * ChartNettyClient
 * @author lgli
 */
public class ChartNettyClient {

    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try{
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new MyChannelInitial());
            Channel channel = bootstrap.connect(new InetSocketAddress("localhost", 8080)).sync().channel();

            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNextLine()){
                String s = scanner.nextLine();
                if("".equals(s)){
                    continue;
                }
                channel.writeAndFlush(s+"\n");
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    static class MyChannelInitial extends ChannelInitializer<SocketChannel>{
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline()
                    .addLast("frame",new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                    .addLast("decode",new StringDecoder())
                    .addLast("encode",new StringEncoder())
                    .addLast("handler",new MyChannelHandler());
        }
    }

    static class MyChannelHandler extends SimpleChannelInboundHandler<String>{
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String o) throws Exception {
            System.out.println(o);
        }
    }
}
