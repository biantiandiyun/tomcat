package com.lzhw.connector;

import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by admin on 2017/5/8.
 */
public abstract class SocketWrapperBase <E>{

    private final Lock blockingStatusReadLock;

    private final Lock blockingStatusWriteLock;

    private final E socket;

    private final AbstractEndpoint<E,?> endpoint;

    public SocketWrapperBase(E socket, AbstractEndpoint<E,?> endpoint) {
        this.socket = socket;
        this.endpoint = endpoint;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.blockingStatusReadLock = lock.readLock();
        this.blockingStatusWriteLock = lock.writeLock();
    }
    public abstract boolean isClosed();
    public E getSocket() {
        return socket;
    }
}
