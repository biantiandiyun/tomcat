package com.lzhw.connector;

import com.lzhw.net.SocketEvent;
import com.lzhw.net.SocketWrapperBase;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Created by admin on 2017/5/8.
 */
public abstract class AbstractProtocol<S> implements ProtocolHandler {
    // ------------------------------------------- Connection handler base class
    private final Map<S, Processor> connections = new ConcurrentHashMap<S, Processor>();

    private AbstractEndpoint.Handler<S> handler;
    /**
     * Endpoint that provides low-level network I/O - must be matched to the
     * ProtocolHandler implementation (ProtocolHandler using NIO, requires NIO
     * Endpoint etc.).
     */
    private final AbstractEndpoint<S, ?> endpoint;
    // ---------------------- Properties that are passed through to the EndPoint
    @Override
    public void init() throws Exception {
        endpoint.init();
    }
    @Override
    public Executor getExecutor() {
        return endpoint.getExecutor();
    }

    public void setExecutor(Executor executor) {
        endpoint.setExecutor(executor);
    }

    public AbstractProtocol(AbstractEndpoint<S, ?> endpoint) {
        this.endpoint = endpoint;
//        setConnectionLinger(Constants.DEFAULT_CONNECTION_LINGER);
//        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
    }

    @Override
    public void start() throws Exception {
        endpoint.start();
    }

    public AbstractEndpoint.Handler<S> getHandler() {
        return handler;
    }

    public void setHandler(AbstractEndpoint.Handler<S> handler) {
        this.handler = handler;
    }

    protected AbstractEndpoint<S, ?> getEndpoint() {
        return endpoint;
    }

    public InetAddress getAddress() { return endpoint.getAddress(); }

    public void setAddress(InetAddress ia) {
        endpoint.setAddress(ia);
    }

    public int getPort() { return endpoint.getPort(); }
    public void setPort(int port) {
        endpoint.setPort(port);
    }
    protected static class ConnectionHandler<S> implements AbstractEndpoint.Handler<S> {
        private final AbstractProtocol<S> proto;

        public ConnectionHandler(AbstractProtocol<S> proto) {
            this.proto = proto;
        }

        protected AbstractProtocol<S> getProtocol() {
            return proto;
        }

        @Override
        public SocketState process(SocketWrapperBase<S> wrapper, SocketEvent status) {
            if (wrapper == null) {
                // Nothing to do. Socket has been closed.
                return SocketState.CLOSED;
            }
            S socket = wrapper.getSocket();


            return null;
        }

        @Override
        public Object getGlobal() {
            return null;
        }

        @Override
        public Set<S> getOpenSockets() {
            return null;
        }

        @Override
        public void release(SocketWrapperBase<S> socketWrapper) {

        }

        @Override
        public void pause() {

        }

        @Override
        public void recycle() {

        }
    }
}
