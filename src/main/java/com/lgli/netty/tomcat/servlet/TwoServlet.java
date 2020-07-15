package com.lgli.netty.tomcat.servlet;

import com.lgli.netty.tomcat.http.NettyRequest;
import com.lgli.netty.tomcat.http.NettyResponse;

/**
 * TwoServlet
 * @author lgli
 */
public class TwoServlet extends NettyServlet{


    protected void doPost(NettyRequest request, NettyResponse response) {
        response.write("This is TwoServlet");
    }

    protected void doGet(NettyRequest request, NettyResponse response) {
        this.doPost(request,response);

    }
}
