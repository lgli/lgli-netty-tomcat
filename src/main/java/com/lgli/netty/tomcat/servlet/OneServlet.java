package com.lgli.netty.tomcat.servlet;

import com.lgli.netty.tomcat.http.NettyRequest;
import com.lgli.netty.tomcat.http.NettyResponse;

/**
 * OneServlet
 * @author lgli
 */
public class OneServlet extends NettyServlet{
    protected void doPost(NettyRequest request, NettyResponse response) {
        response.write("This is OneSerlvet");
    }

    protected void doGet(NettyRequest request, NettyResponse response) {

        this.doPost(request,response);

    }
}
