package com.lzhw.connector;

/**
 * Created by admin on 2017/5/9.
 */
public class Acceptor<U> implements Runnable{

    private static final int INITIAL_ERROR_DELAY = 50;
    private static final int MAX_ERROR_DELAY = 1600;

    private final AbstractEndpoint<?,U> endpoint;
    private String threadName;
    public Acceptor(AbstractEndpoint<?,U> endpoint) {
        this.endpoint = endpoint;
    }
    @Override
    public void run() {

    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadName() {
        return threadName;
    }
}
