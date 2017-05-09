package com.lzhw.connector.nio;

import com.lzhw.connector.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by admin on 2017/5/8.
 */
public abstract class NioEndpoint extends AbstractEndpoint {

    /**
     * The socket poller.
     */
    private Poller[] pollers = null;

    private long selectorTimeout = 1000;

    public void startInternal()  {
        if (!running) {
            running = true;
            paused = false;
            // Create worker collection
            if (getExecutor() == null) {
                createExecutor();
            }
        }
    }

    /**
     * Poller class.
     */
    public class Poller implements Runnable {
        private Selector selector;
        private volatile boolean close = false;
        private long nextExpiration = 0;//optimize expiration handling

        private AtomicLong wakeupCounter = new AtomicLong(0);

        private volatile int keyCount = 0;

        public Poller() throws IOException {
            this.selector = Selector.open();
        }

        @Override
        public void run() {
            while (true) {
                boolean hasEvents = false;
                try {
                    if (!close) {
                        hasEvents = events();
                        if (wakeupCounter.getAndSet(-1) > 0) {
                            //if we are here, means we have other stuff to do
                            //do a non blocking select
                            keyCount = selector.selectNow();
                        } else {
                            keyCount = selector.select(selectorTimeout);
                        }
                        wakeupCounter.set(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //either we timed out or we woke up, process events first
                if (keyCount == 0) hasEvents = (hasEvents | events());
                Iterator<SelectionKey> iterator = keyCount > 0 ? selector.selectedKeys().iterator() : null;
                // Walk through the collection of ready keys and dispatch
                // any active event.
                while (iterator != null && iterator.hasNext()) {
                    SelectionKey sk = iterator.next();
                    NioSocketWrapper attachment = (NioSocketWrapper) sk.attachment();
                    // Attachment may be null if another thread has called
                    // cancelledKey()
                    if (attachment == null) {
                        iterator.remove();
                    } else {
                        iterator.remove();
                        processKey(sk, attachment);
                    }
                }

            }
        }

        protected void processKey(SelectionKey sk, NioSocketWrapper attachment) {
            if (close) {
            } else if (sk.isValid() && attachment != null) {
                if (sk.isReadable() || sk.isWritable()) {
                    if (attachment.getSendfileData() != null) {
//                        processSendfile(sk,)
                    } else {

                    }
                }
            }
        }

        public boolean events() {
            return true;
        }

        public int getKeyCount() {
            return keyCount;
        }

        public Selector getSelector() {
            return selector;
        }
    }

    public static class NioSocketWrapper extends SocketWrapperBase<NioChannel> {
//        private final NioSelectorPool pool;

        private Poller poller = null;
        private int interestOps = 0;
        private CountDownLatch readLatch = null;
        private CountDownLatch writeLatch = null;
        private volatile SendfileData sendfileData = null;
        private volatile long lastRead = System.currentTimeMillis();
        private volatile long lastWrite = lastRead;

        public NioSocketWrapper(NioChannel channel, NioEndpoint endpoint) {
            super(channel, endpoint);
//            pool = endpoint.getSelectorPool();
//            socketBufferHandler = channel.getBufHandler();
        }

        public SendfileData getSendfileData() {
            return this.sendfileData;
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }
// ----------------------------------------------- SendfileData Inner Class

    /**
     * SendfileData class.
     */
    public static class SendfileData extends SendfileDataBase {

        public SendfileData(String filename, long pos, long length) {
            super(filename, pos, length);
        }

        protected volatile FileChannel fchannel;
    }

    /**
     * This class is the equivalent of the Worker, but will simply use in an
     * external Executor thread pool.
     */
    protected class SocketProcessor extends SocketProcessorBase<NioChannel> {

        public SocketProcessor(SocketWrapperBase<NioChannel> socketWrapper, SocketEvent event) {
            super(socketWrapper, event);
        }

        @Override
        protected void doRun() {
            NioChannel socket = socketWrapper.getSocket();
            SelectionKey key = socket.getIOChannel().keyFor(socket.getPoller().getSelector());
            int handshake = -1;
            if (key != null) {
                if (socket.isHandshakeComplete()) {
                    // No TLS handshaking required. Let the handler
                    // process this socket / event combination.
                    handshake = 0;
                } else if (event == SocketEvent.STOP || event == SocketEvent.DISCONNECT
                        || event == SocketEvent.ERROR) {
                    // Unable to complete the TLS handshake. Treat it as
                    // if the handshake failed.
                    handshake = -1;
                } else {
                    try {
                        handshake = socket.handshake(key.isReadable(), key.isWritable());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // The handshake process reads/writes from/to the
                    // socket. status may therefore be OPEN_WRITE once
                    // the handshake completes. However, the handshake
                    // happens when the socket is opened so the status
                    // must always be OPEN_READ after it completes. It
                    // is OK to always set this as it is only used if
                    // the handshake completes.
                    event = SocketEvent.OPEN_READ;
                }
            }
            if (handshake == 0) {
                Handler.SocketState state = Handler.SocketState.OPEN;
                // Process the request from this socket
                if (event == null) {
                    state = getHandler().process(socketWrapper, SocketEvent.OPEN_READ);
                } else {
                    state = getHandler().process(socketWrapper, event);
                }
                if (state == Handler.SocketState.CLOSED) {
                    close(socket, key);
                }
            }
        }

        private void close(NioChannel socket, SelectionKey key) {

        }

    }
}
