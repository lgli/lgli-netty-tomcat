package com.lgli.netty.tomcat.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * NettyRequest
 * @author lgli
 */
public class NettyRequest {

    private ChannelHandlerContext ctx;

    private HttpRequest request;


    public NettyRequest(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) {
        this.ctx = channelHandlerContext;
        this.request = httpRequest;
    }

    public String getRequestMethod() {
        return request.method().name();
    }

    public String getUrl() {
        return request.uri();
    }
}
