package com.lgli.netty.tomcat;

import com.lgli.netty.tomcat.http.NettyRequest;
import com.lgli.netty.tomcat.http.NettyResponse;
import com.lgli.netty.tomcat.servlet.NettyServlet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * NettyTomcat
 * @author lgli
 */
public class NettyTomcat {

    private static Map<String, NettyServlet> maps = new HashMap<>(16);

    public NettyTomcat() {
        try{
            //初始化资源信息
            Properties properties = new Properties();
            InputStream in = NettyTomcat.class.getClassLoader().getResourceAsStream("netty-tomcat.properties");
            properties.load(in);
            Enumeration<?> enumeration = properties.propertyNames();
            while(enumeration.hasMoreElements()){
                Object o = enumeration.nextElement();
                if(!(o instanceof String)){
                    continue;
                }
                String url = (String)o;
                if(!url.endsWith("-url")){
                    continue;
                }
                Object urlClass = Class.forName(properties.getProperty(url.replace("url", "class"))).newInstance();
                if(!(urlClass instanceof NettyServlet)){
                    continue;
                }
                maps.put(properties.getProperty(url),(NettyServlet) urlClass);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void monitor() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try{
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new NettyTomcatChannelInitial());
            Channel channel = serverBootstrap.bind(new InetSocketAddress("localhost", 8080)).sync().channel();
            channel.closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class NettyTomcatChannelInitial extends ChannelInitializer<SocketChannel>{
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast("encode",new HttpResponseEncoder())
                    .addLast("decode",new HttpRequestDecoder())
                    .addLast("handler",new NettyTomcatHandler());
        }
    }

    static class NettyTomcatHandler extends SimpleChannelInboundHandler<Object>{

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            if(!(o instanceof HttpRequest)){
                return;
            }
            HttpRequest httpRequest = (HttpRequest) o;
            NettyRequest request = new NettyRequest(channelHandlerContext,httpRequest);
            NettyResponse response = new NettyResponse(channelHandlerContext);
            String url = request.getUrl();
            if(maps.containsKey(url)){
                maps.get(url).service(request,response);
            }else{
                response.write("404 Not Found,找不到资源");
            }
        }
    }




    public static void main(String[] args) {
        new NettyTomcat().monitor();
    }




}
