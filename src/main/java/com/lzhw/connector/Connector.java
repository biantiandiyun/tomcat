package com.lzhw.connector;

import com.lzhw.connector.nio.Http11NioProtocol;
import com.lzhw.core.LifecycleBase;

/**
 * Created by admin on 2017/5/5.
 */
public class Connector extends LifecycleBase {

    /**
     * Coyote protocol handler.
     */
    protected final ProtocolHandler protocolHandler;

    public Connector(ProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    /**
     * The port number on which we listen for requests.
     */

    protected int port = -1;

    @Override
    protected void initInternal() throws Exception {
        protocolHandler.init();
    }

    @Override
    protected void startInternal() throws Exception {
        try {
            protocolHandler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @return the port number on which this connector is configured to listen
     * for requests. The special value of 0 means select a random free port
     * when the socket is bound.
     */
    public int getPort() {
        return this.port;
    }


    /**
     * Set the port number on which we listen for requests.
     *
     * @param port The new port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        Http11NioProtocol http11NioProtocol = new Http11NioProtocol();
        http11NioProtocol.setPort(8000);
        Connector connector = new Connector(new Http11NioProtocol());
        connector.setPort(8000);
        try {
            connector.initInternal();
            connector.startInternal();
            Thread.sleep(100000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
