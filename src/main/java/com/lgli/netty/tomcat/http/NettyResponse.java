package com.lgli.netty.tomcat.http;

import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * NettyResponse
 * @author lgli
 */
public class NettyResponse {

    private ChannelHandlerContext ctx;



    public NettyResponse(ChannelHandlerContext channelHandlerContext) {
        this.ctx = channelHandlerContext;
    }


    public void write(String result) {
        if(null == result || "".equalsIgnoreCase(result)
                || "".equalsIgnoreCase(result.trim())){
            return;
        }
        try{
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(result.getBytes("GBK"))
            );
            response.headers().set("Content-Type","text/html");
            ctx.write(response);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ctx.flush();
            ctx.close();
        }
    }
}
