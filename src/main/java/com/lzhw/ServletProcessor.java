package com.lzhw;

import javax.servlet.Servlet;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

/**
 * Created by admin on 2017/3/13.
 */
public class ServletProcessor {
    public void process(Request request,Response response){
        String uri = request.getUri().toString();
        String servletName = uri.substring(uri.lastIndexOf("/")+1);
        URLClassLoader loader = null;
        URL[] urls = new URL[1];
        URLStreamHandler streamHandler = null;
        File classPath = new File(Bootstrap.WEB_ROOT);
        try {
            String repository = new URL("file",null,classPath.getCanonicalPath()+File.separator).toString();
            urls[0] = new URL(null,repository,streamHandler);
            loader = new URLClassLoader(urls);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Class myclass = null;
        try {
            myclass = loader.loadClass(servletName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Servlet servlet = null;
        RequestFacade requestFacade =  new RequestFacade(request);
        ResponseFacade responseFacade = new ResponseFacade(response);
        try {
            servlet = (Servlet) myclass.newInstance();
            servlet.service(requestFacade,responseFacade);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
