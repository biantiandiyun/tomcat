package com.lzhw.connector;

import com.lzhw.connector.apr.Http11AprProtocol;
import com.lzhw.connector.nio.Http11NioProtocol;

/**
 * Created by admin on 2017/5/5.
 */
public class Connector {

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

    public void start() {
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
        Connector connector = new Connector(new Http11AprProtocol());
        connector.start();
    }
}
