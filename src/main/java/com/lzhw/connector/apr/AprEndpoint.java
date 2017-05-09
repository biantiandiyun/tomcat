package com.lzhw.connector.apr;

import com.lzhw.connector.AbstractEndpoint;
import sun.rmi.runtime.Log;

/**
 * Created by admin on 2017/5/9.
 */
public class AprEndpoint extends AbstractEndpoint<Long, Long> {
    /**
     * The socket poller.
     */
    protected Poller poller = null;
    /**
     * The static file sender.
     */
    protected Sendfile sendfile = null;

    public Sendfile getSendfile() {
        return sendfile;
    }
    public Poller getPoller() {
        return poller;
    }

    /**
     * Start the APR endpoint, creating acceptor, poller and sendfile threads.
     */
    public void startInternal() throws Exception {
        if (!running) {
            running = true;
            paused = false;

            // Create worker collection
            if (getExecutor() == null) {
                createExecutor();
            }
            initializeConnectionLatch();
            poller = new Poller();
            Thread pollerThread = new Thread(poller, getName() + "-Poller");
            pollerThread.setPriority(threadPriority);
            pollerThread.setDaemon(true);
            pollerThread.start();
            // Start sendfile thread
            if (getUseSendfile()) {
                sendfile = new Sendfile();
                sendfile.init();
                Thread sendfileThread = new Thread(sendfile, getName() + "-Sendfile");
                sendfileThread.setPriority(threadPriority);
                sendfileThread.setDaemon(true);
                sendfileThread.start();
            }
            startAcceptorThreads();
        }
    }
    public class Sendfile implements Runnable {

        public void init(){

        }
        @Override
        public void run() {

        }
    }
    public class Poller implements  Runnable{

        public void init(){

        }
        @Override
        public void run() {

        }
    }
}
