package com.lzhw;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collection;
import java.util.Locale;

/**
 * Created by admin on 2017/3/10.
 */
public class Response implements HttpServletResponse{

    private HttpExchange httpExchange;

    private ServletOutputStream outputStream;
    private Request request;
    private long length;
    private PrintWriter writer;
    private Headers headers;
    public Response() {

    }

    public Response(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        headers = httpExchange.getResponseHeaders();
        this.outputStream = new MyServletOutputStream(httpExchange.getResponseBody());
    }

    public void sendResponseHeaders(){
        try {
            httpExchange.sendResponseHeaders(200,length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendStaticResource() {
        File file = new File(Bootstrap.WEB_ROOT + request.getUri());
        if (!file.exists()) {
            return;
        }
        BufferedInputStream bufferedInputStream = null;
        try {

            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(bufferedInputStream, "utf-8"));
            StringBuffer stringBuffer = new StringBuffer();
            String tmp = "";
            while ((tmp = br.readLine()) != null) {
                stringBuffer.append(tmp);
            }
            this.length = stringBuffer.toString().getBytes().length;
            sendResponseHeaders();
//            outputStream.write(stringBuffer.toString().getBytes());
              write(stringBuffer.toString());
//            getWriter().flush();
//            getWriter().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                outputStream.close();
                request.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public ServletOutputStream getOutputStream() {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(outputStream,"UTF-8"),true);
        return writer;
    }
    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }
    public void setCharacterEncoding(String charset) {

    }

    public void setContentLength(int len) {
        this.length = len;
    }

    public void setContentLengthLong(long len) {

    }

    public void setContentType(String type) {

    }

    public void setBufferSize(int size) {

    }

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {

    }

    public void resetBuffer() {

    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {

    }

    public void setLocale(Locale loc) {

    }

    public Locale getLocale() {
        return null;
    }

    public void setOutputStream(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {

    }

    @Override
    public void setStatus(int sc, String sm) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }
}
