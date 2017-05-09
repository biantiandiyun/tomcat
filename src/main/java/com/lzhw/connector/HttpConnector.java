package com.lzhw.connector;

import com.lzhw.Request;
import com.lzhw.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by admin on 2017/4/24.
 */
public class HttpConnector {

    public void run() {
        HttpServer httpServer = null;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080),0);
            httpServer.createContext("/webapp", new HttpHandler() {
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
                    HttpProcessor processor = new HttpProcessor(HttpConnector.this);
                    processor.process(httpExchange);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpServer.start();

    }
}
