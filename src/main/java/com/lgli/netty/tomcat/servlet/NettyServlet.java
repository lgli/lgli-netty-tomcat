package com.lgli.netty.tomcat.servlet;

import com.lgli.netty.tomcat.http.NettyRequest;
import com.lgli.netty.tomcat.http.NettyResponse;

/**
 * NettyServlet
 * @author lgli
 */
public abstract class NettyServlet {

    public void service(NettyRequest request, NettyResponse response) throws Exception{
        String method = request.getRequestMethod();
        if("GET".equalsIgnoreCase(method)){
            doGet(request,response);
        }else if("POST".equalsIgnoreCase(method)){
            doPost(request,response);
        }else{
            response.write("暂时不支持的请求类型");
        }
    }

    protected abstract void doPost(NettyRequest request, NettyResponse response);

    protected abstract void doGet(NettyRequest request, NettyResponse response);
}
