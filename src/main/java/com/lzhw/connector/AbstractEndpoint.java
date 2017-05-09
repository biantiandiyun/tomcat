package com.lzhw.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by admin on 2017/5/8.
 */
public abstract class AbstractEndpoint<S, U> {
    /**
     * Running state of the endpoint.
     */
    protected volatile boolean running = false;
    /**
     * Will be set to true whenever the endpoint is paused.
     */
    protected volatile boolean paused = false;
    /**
     * External Executor based thread pool.
     */
    private Executor executor = null;

    /**
     * Are we using an internal executor
     */
    protected volatile boolean internalExecutor = true;

    private int maxConnections = 10000;
    /**
     * Handling of accepted sockets.
     */
    private Handler<S> handler = null;
    private boolean useSendfile = true;
    /**
     * Priority of the worker threads.
     */
    protected int threadPriority = Thread.NORM_PRIORITY;
    /**
     * Acceptor thread count.
     */
    protected int acceptorThreadCount = 1;
    /**
     * Threads used to accept new connections and pass them to worker threads.
     */
    protected List<Acceptor<U>> acceptors;
    /**
     * Priority of the acceptor threads.
     */
    protected int acceptorThreadPriority = Thread.NORM_PRIORITY;
    /**
     * The default is true - the created threads will be
     * in daemon mode. If set to false, the control thread
     * will not be daemon - and will keep the process alive.
     */
    private boolean daemon = true;

    public void setDaemon(boolean b) {
        daemon = b;
    }

    public boolean getDaemon() {
        return daemon;
    }

    public void setAcceptorThreadPriority(int acceptorThreadPriority) {
        this.acceptorThreadPriority = acceptorThreadPriority;
    }

    public int getAcceptorThreadPriority() {
        return acceptorThreadPriority;
    }

    public void setAcceptorThreadCount(int acceptorThreadCount) {
        this.acceptorThreadCount = acceptorThreadCount;
    }

    public int getAcceptorThreadCount() {
        return acceptorThreadCount;
    }

    public void setThreadPriority(int threadPriority) {
        // Can't change this once the executor has started
        this.threadPriority = threadPriority;
    }

    public int getThreadPriority() {
        if (internalExecutor) {
            return threadPriority;
        } else {
            return -1;
        }
    }

    /**
     * Name of the thread pool, which will be used for naming child threads.
     */
    private String name = "TP";

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean getUseSendfile() {
        return useSendfile;
    }

    public void setUseSendfile(boolean useSendfile) {
        this.useSendfile = useSendfile;
    }

    public void setHandler(Handler<S> handler) {
        this.handler = handler;
    }

    public Handler<S> getHandler() {
        return handler;
    }

    public void createExecutor() {
        internalExecutor = true;
        executor = Executors.newFixedThreadPool(100);
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    protected void initializeConnectionLatch() {

    }

    protected final void startAcceptorThreads() {
        int count = getAcceptorThreadCount();
        acceptors = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Acceptor<U> acceptor = new Acceptor<>(this);
            String threadName = getName() + "-Acceptor-" + i;
            acceptor.setThreadName(threadName);
            acceptors.add(acceptor);
            Thread t = new Thread(acceptor, threadName);
            t.setPriority(getAcceptorThreadPriority());
            t.setDaemon(getDaemon());
            t.start();
        }
    }
    public final void start() throws Exception {
        startInternal();
    }
    public abstract void startInternal() throws Exception;
    public Executor getExecutor() {
        return executor;
    }

    public static interface Handler<S> {

        /**
         * Different types of socket states to react upon.
         */
        public enum SocketState {
            // TODO Add a new state to the AsyncStateMachine and remove
            //      ASYNC_END (if possible)
            OPEN, CLOSED, LONG, ASYNC_END, SENDFILE, UPGRADING, UPGRADED
        }


        /**
         * Process the provided socket with the given current status.
         *
         * @param socket The socket to process
         * @param status The current socket status
         * @return The state of the socket after processing
         */
        public SocketState process(SocketWrapperBase<S> socket,
                                   SocketEvent status);


        /**
         * Obtain the GlobalRequestProcessor associated with the handler.
         *
         * @return the GlobalRequestProcessor
         */
        public Object getGlobal();


        /**
         * Obtain the currently open sockets.
         *
         * @return The sockets for which the handler is tracking a currently
         * open connection
         */
        public Set<S> getOpenSockets();

        /**
         * Release any resources associated with the given SocketWrapper.
         *
         * @param socketWrapper The socketWrapper to release resources for
         */
        public void release(SocketWrapperBase<S> socketWrapper);


        /**
         * Inform the handler that the endpoint has stopped accepting any new
         * connections. Typically, the endpoint will be stopped shortly
         * afterwards but it is possible that the endpoint will be resumed so
         * the handler should not assume that a stop will follow.
         */
        public void pause();


        /**
         * Recycle resources associated with the handler.
         */
        public void recycle();


    }

}
