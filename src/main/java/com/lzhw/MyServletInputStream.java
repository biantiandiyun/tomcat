package com.lzhw;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by admin on 2017/3/13.
 */
public class MyServletInputStream extends ServletInputStream {

    private  InputStream inputStream;

    public  MyServletInputStream(){

    }
    public  MyServletInputStream(InputStream inputStream){
        this.inputStream = inputStream;
    }
    public boolean isFinished() {
        return false;
    }

    public boolean isReady() {

        return false;
    }

    public void setReadListener(ReadListener readListener) {

    }

    public int read() throws IOException {
        return  inputStream.read();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
