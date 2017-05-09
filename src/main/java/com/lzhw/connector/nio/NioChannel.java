package com.lzhw.connector.nio;

import com.lzhw.connector.SocketWrapperBase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by admin on 2017/5/8.
 */
public class NioChannel implements ByteChannel {

    protected static final ByteBuffer emptyBuf = ByteBuffer.allocate(0);

    protected SocketChannel sc = null;
    protected SocketWrapperBase<NioChannel> socketWrapper = null;

//    protected final SocketBufferHandler bufHandler;

    protected NioEndpoint.Poller poller;

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return 0;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return 0;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
    public SocketChannel getIOChannel() {
        return sc;
    }
    public NioEndpoint.Poller getPoller() {
        return poller;
    }
    public boolean isHandshakeComplete() {
        return true;
    }
    /**
     * Performs SSL handshake hence is a no-op for the non-secure
     * implementation.
     *
     * @param read  Unused in non-secure implementation
     * @param write Unused in non-secure implementation
     * @return Always returns zero
     * @throws IOException Never for non-secure channel
     */
    public int handshake(boolean read, boolean write) throws IOException {
        return 0;
    }
}
