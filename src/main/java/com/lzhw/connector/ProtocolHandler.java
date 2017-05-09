package com.lzhw.connector;

import java.util.concurrent.Executor;

/**
 * Created by admin on 2017/5/8.
 */
public interface ProtocolHandler {
    /**
     * The executor, provide access to the underlying thread pool.
     *
     * @return The executor used to process requests
     */
    public Executor getExecutor();


    /**
     * Initialise the protocol.
     *
     * @throws Exception If the protocol handler fails to initialise
     */
    public void init() throws Exception;
    /**
     * Start the protocol.
     *
     * @throws Exception If the protocol handler fails to start
     */
    public void start() throws Exception;
}
