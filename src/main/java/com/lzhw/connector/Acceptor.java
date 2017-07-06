package com.lzhw.connector;

/**
 * Created by admin on 2017/5/9.
 */
public class Acceptor<U> implements Runnable{

    private static final int INITIAL_ERROR_DELAY = 50;
    private static final int MAX_ERROR_DELAY = 1600;
    protected volatile AcceptorState state = AcceptorState.NEW;

    private final AbstractEndpoint<?,U> endpoint;
    private String threadName;
    public Acceptor(AbstractEndpoint<?,U> endpoint) {
        this.endpoint = endpoint;
    }
    @Override
    public void run() {
        int errorDelay = 0;
        // Loop until we receive a shutdown command
        while (endpoint.isRunning()) {
            // Loop if endpoint is paused
            while (endpoint.isPaused() && endpoint.isRunning()) {
                state = AcceptorState.PAUSED;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            if (!endpoint.isRunning()) {
                break;
            }
            state = AcceptorState.RUNNING;

            U socket = null;
            try {
                //if we have reached max connections, wait
//                endpoint.countUpOrAwaitConnection();
                // Accept the next incoming connection from the server
                // socket
                socket = endpoint.serverSocketAccept();
            } catch (Exception ioe) {

            }
            // Configure the socket
            if (endpoint.isRunning() && !endpoint.isPaused()) {
                // setSocketOptions() will hand the socket off to
                // an appropriate processor if successful
                if (!endpoint.setSocketOptions(socket)) {
                    endpoint.closeSocket(socket);
                }
            } else {
                endpoint.destroySocket(socket);
            }



        }
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadName() {
        return threadName;
    }

    public enum AcceptorState {
        NEW, RUNNING, PAUSED, ENDED
    }
}
