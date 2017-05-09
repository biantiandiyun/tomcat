package com.lzhw.connector;

import com.lzhw.Request;
import com.lzhw.Response;
import com.lzhw.ServletProcessor;
import com.lzhw.StaticResourceProcessor;
import com.sun.net.httpserver.HttpExchange;

/**
 * Created by admin on 2017/4/25.
 */
public class HttpProcessor {
    private HttpConnector connector;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public HttpProcessor() {
    }

    public  void process(HttpExchange httpExchange){
        Request request = new Request(httpExchange);
        request.setUri(httpExchange.getRequestURI());
        request.parse();
        Response response = new Response(httpExchange);
        response.setRequest(request);
        if (request.getUri().toString().startsWith("servlet")){
            ServletProcessor servletProcessor = new ServletProcessor();
            servletProcessor.process(request,response);
        }else {
            StaticResourceProcessor staticResourceProcessor = new StaticResourceProcessor();
            staticResourceProcessor.process(request,response);
        }
    }
}
