package com.lzhw;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * Created by admin on 2017/3/10.
 */
public class Bootstrap {

    public static final  String WEB_ROOT = System.getProperty("user.dir")
            +"/tomcat/src/main/"+ File.separator;

    public static void main(String[] args) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(8081),0);
            httpServer.createContext("/webapp", new HttpHandler() {
                public void handle(HttpExchange t) throws IOException {
                    System.out.println("handler:"+t.getRequestMethod()+":"+t.getRequestURI());

                    Request request = new Request(t);
                    request.setUri(t.getRequestURI());
                    request.parse();
                    Response response = new Response(t);
                    response.setRequest(request);
//                    response.sendStaticResource();
                    ServletProcessor processor = new ServletProcessor();
                    processor.process(request,response);
                }
            });
            httpServer.start();
            System.out.println("server start");
            System.out.println("web root:"+WEB_ROOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
