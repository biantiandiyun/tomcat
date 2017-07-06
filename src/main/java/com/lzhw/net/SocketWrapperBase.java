package com.lzhw.net;

import com.lzhw.connector.AbstractEndpoint;

import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by admin on 2017/5/8.
 */
public abstract class SocketWrapperBase <E>{

    private final Lock blockingStatusReadLock;

    private final Lock blockingStatusWriteLock;
    // Volatile because I/O and setting the timeout values occurs on a different
    // thread to the thread checking the timeout.
    private volatile long readTimeout = -1;
    private volatile long writeTimeout = -1;

    private volatile int keepAliveLeft = 100;
    private boolean secure = false;
    /**
     * The buffers used for communicating with the socket.
     */
    protected volatile SocketBufferHandler socketBufferHandler = null;
    private final E socket;

    private final AbstractEndpoint<E,?> endpoint;

    public SocketWrapperBase(E socket, AbstractEndpoint<E,?> endpoint) {
        this.socket = socket;
        this.endpoint = endpoint;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.blockingStatusReadLock = lock.readLock();
        this.blockingStatusWriteLock = lock.writeLock();
    }

    public AbstractEndpoint<E, ?> getEndpoint() {
        return endpoint;
    }

    public abstract boolean isClosed();
    public E getSocket() {
        return socket;
    }

    public Lock getBlockingStatusReadLock() {
        return blockingStatusReadLock;
    }

    public Lock getBlockingStatusWriteLock() {
        return blockingStatusWriteLock;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getKeepAliveLeft() {
        return keepAliveLeft;
    }

    public void setKeepAliveLeft(int keepAliveLeft) {
        this.keepAliveLeft = keepAliveLeft;
    }

    public SocketBufferHandler getSocketBufferHandler() {
        return socketBufferHandler;
    }

    public void setSocketBufferHandler(SocketBufferHandler socketBufferHandler) {
        this.socketBufferHandler = socketBufferHandler;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
