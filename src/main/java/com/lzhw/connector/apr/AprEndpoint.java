package com.lzhw.connector.apr;

import com.lzhw.connector.AbstractEndpoint;
import sun.rmi.runtime.Log;

/**
 * Created by admin on 2017/5/9.
 */
public class AprEndpoint extends AbstractEndpoint<Long, Long> {

    @Override
    protected Long serverSocketAccept() throws Exception {
        return null;
    }

    @Override
    protected boolean setSocketOptions(Long socket) {
        return false;
    }

    @Override
    protected void closeSocket(Long socket) {

    }

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

    @Override
    public void bind() throws Exception {

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

        private volatile boolean pollerRunning = true;

        public void init(){

        }
        @Override
        public void run() {
            SocketList localAddList = new SocketList(getMaxConnections());
            SocketList localCloseList = new SocketList(getMaxConnections());
            while (pollerRunning){

            }
        }
    }
    public static class SocketInfo {
        public long socket;
        public long timeout;
        public int flags;


    }
    public static class SocketList{

        protected volatile int size;
        protected int pos;

        protected long[] sockets;
        protected long[] timeouts;
        protected int[] flags;

        protected SocketInfo info = new SocketInfo();

        public SocketList(int size) {
            this.size = 0;
            pos = 0;
            sockets = new long[size];
            timeouts = new long[size];
            flags = new int[size];
        }

        public int size() {
            return this.size;
        }

        public SocketInfo get() {
            if (pos == size) {
                return null;
            } else {
                info.socket = sockets[pos];
                info.timeout = timeouts[pos];
                info.flags = flags[pos];
                pos++;
                return info;
            }
        }

        public void clear() {
            size = 0;
            pos = 0;
        }

        public boolean add(long socket, long timeout, int flag) {
            if (size == sockets.length) {
                return false;
            } else {
                for (int i = 0; i < size; i++) {
                    if (sockets[i] == socket) {
//                        flags[i] = SocketInfo.merge(flags[i], flag);
                        return true;
                    }
                }
                sockets[size] = socket;
                timeouts[size] = timeout;
                flags[size] = flag;
                size++;
                return true;
            }
        }

        public boolean remove(long socket) {
            for (int i = 0; i < size; i++) {
                if (sockets[i] == socket) {
                    sockets[i] = sockets[size - 1];
                    timeouts[i] = timeouts[size - 1];
                    flags[size] = flags[size -1];
                    size--;
                    return true;
                }
            }
            return false;
        }

        public void duplicate(SocketList copy) {
            copy.size = size;
            copy.pos = pos;
            System.arraycopy(sockets, 0, copy.sockets, 0, size);
            System.arraycopy(timeouts, 0, copy.timeouts, 0, size);
            System.arraycopy(flags, 0, copy.flags, 0, size);
        }


    }
}
