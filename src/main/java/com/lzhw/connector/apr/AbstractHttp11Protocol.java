package com.lzhw.connector.apr;

import com.lzhw.connector.AbstractEndpoint;
import com.lzhw.connector.AbstractProtocol;

/**
 * Created by admin on 2017/5/9.
 */
public abstract class AbstractHttp11Protocol<S> extends AbstractProtocol<S> {

    private String server;

    public AbstractHttp11Protocol(AbstractEndpoint<S,?> endpoint) {
        super(endpoint);
//        setConnectionTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        ConnectionHandler<S> cHandler = new ConnectionHandler<S>(this);
        setHandler(cHandler);
        getEndpoint().setHandler(cHandler);
    }
    /**
     * {@inheritDoc}
     * <p>
     * Over-ridden here to make the method visible to nested classes.
     */
    protected AbstractEndpoint<S,?> getEndpoint() {
        return super.getEndpoint();
    }
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
