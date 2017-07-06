package com.lzhw.connector.nio;

import com.lzhw.connector.*;
import com.lzhw.net.*;
import com.lzhw.util.SynchronizedQueue;
import com.lzhw.util.SynchronizedStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by admin on 2017/5/8.
 */
public class NioEndpoint extends AbstractEndpoint<NioChannel, SocketChannel> {

  private static final Logger logger = LoggerFactory.getLogger(NioEndpoint.class);

    public static final int OP_REGISTER = 0x100; //register interest op
    /**
     * The socket poller.
     */
    private Poller[] pollers = null;

    private AtomicInteger pollerRotater = new AtomicInteger(0);
    /**
     * Return an available poller in true round robin fashion.
     *
     * @return The next poller in sequence
     */
    public Poller getPoller0() {
        int idx = Math.abs(pollerRotater.incrementAndGet()) % pollers.length;
        return pollers[idx];
    }
    private NioSelectorPool selectorPool = new NioSelectorPool();

    private long selectorTimeout = 1000;
    /**
     * Poller thread count.
     */
    private int pollerThreadCount = Math.min(2, Runtime.getRuntime().availableProcessors());
    /**
     * Cache for poller events
     */
    private SynchronizedStack<PollerEvent> eventCache;
    /**
     * Bytebuffer cache, each channel holds a set of buffers (two, except for SSL holds four)
     */
    private SynchronizedStack<NioChannel> nioChannels;
    /**
     * Server socket "pointer".
     */
    private ServerSocketChannel serverSock = null;

    private volatile CountDownLatch stopLatch = null;

    protected SocketChannel sc = null;

    protected CountDownLatch getStopLatch() {
        return stopLatch;
    }


    protected void setStopLatch(CountDownLatch stopLatch) {
        this.stopLatch = stopLatch;
    }
    public void setPollerThreadCount(int pollerThreadCount) {
        this.pollerThreadCount = pollerThreadCount;
    }

    @Override
    protected SocketChannel serverSocketAccept() throws Exception {
        return serverSock.accept();
    }
    /**
     * Process the specified connection.
     * @param socket The socket channel
     * @return <code>true</code> if the socket was correctly configured
     *  and processing may continue, <code>false</code> if the socket needs to be
     *  close immediately
     */
    @Override
    protected boolean setSocketOptions(SocketChannel socket) {
        try {
            socket.configureBlocking(false);
            Socket sock = socket.socket();
            socketProperties.setProperties(sock);
            NioChannel channel = nioChannels.pop();
            if (channel==null){
                SocketBufferHandler bufferHandler = new SocketBufferHandler(socketProperties.getAppReadBufSize(),
                        socketProperties.getAppWriteBufSize(),
                        socketProperties.getDirectBuffer());
                channel = new NioChannel(socket, bufferHandler);
            }else {
                channel.setIOChannel(socket);
                channel.reset();
            }
            getPoller0().register(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void bind() throws Exception {
        serverSock = ServerSocketChannel.open();
        socketProperties.setProperties(serverSock.socket());
        InetSocketAddress addr = (getAddress() != null ? new InetSocketAddress(getAddress(), getPort()) : new InetSocketAddress(getPort()));
        logger.info("服务地址："+addr.getHostName()+":"+addr.getPort());
        serverSock.socket().bind(addr, getAcceptCount());
        serverSock.configureBlocking(true); //mimic APR behavior
        // Initialize thread count defaults for acceptor, poller
        if (acceptorThreadCount == 0) {
            // FIXME: Doesn't seem to work that well with multiple accept threads
            acceptorThreadCount = 1;
        }
        if (pollerThreadCount <= 0) {
            //minimum one poller thread
            pollerThreadCount = 1;
        }
        setStopLatch(new CountDownLatch(pollerThreadCount));
        selectorPool.open();
        // Initialize SSL if needed
//        initialiseSsl();
    }

    public static void main(String[] args) throws Exception {
        NioEndpoint nioEndpoint = new NioEndpoint();
        nioEndpoint.setPort(8000);
        nioEndpoint.bind();
//        nioEndpoint.startInternal();
        Thread.sleep(1000000);
    }

    public int getPollerThreadCount() {
        return pollerThreadCount;
    }

    public void startInternal() throws Exception {
        if (!running) {
            running = true;
            paused = false;
            processorCache = new SynchronizedStack<>(SynchronizedStack.DEFAULT_SIZE,
                    socketProperties.getProcessorCache());
            eventCache = new SynchronizedStack<>(SynchronizedStack.DEFAULT_SIZE,
                    socketProperties.getEventCache());
            nioChannels = new SynchronizedStack<>(SynchronizedStack.DEFAULT_SIZE,
                    socketProperties.getBufferPool());
            // Create worker collection
            if (getExecutor() == null) {
                createExecutor();
            }
            initializeConnectionLatch();
            pollers = new Poller[getPollerThreadCount()];
            for (int i = 0; i < pollers.length; i++) {
                pollers[i] = new Poller();
                Thread pollerThread = new Thread(pollers[i], getName() + "-ClientPoller-" + i);
                pollerThread.setPriority(threadPriority);
                pollerThread.setDaemon(true);
                pollerThread.start();
            }
            startAcceptorThreads();
        }
    }

    @Override
    protected void closeSocket(SocketChannel socket) {
//        countDownConnection();
        try {
            socket.socket().close();
        } catch (IOException ioe)  {
//            if (log.isDebugEnabled()) {
//                log.debug(sm.getString("endpoint.err.close"), ioe);
//            }
        }
        try {
            socket.close();
        } catch (IOException ioe) {
//            if (log.isDebugEnabled()) {
//                log.debug(sm.getString("endpoint.err.close"), ioe);
//            }
        }
    }

    /**
     * PollerEvent, cacheable object for poller events to avoid GC
     */
    public static class PollerEvent implements Runnable {
        private NioChannel socket;
        private int interestOps;
        private NioSocketWrapper socketWrapper;

        public PollerEvent(NioChannel ch, NioSocketWrapper w, int intOps) {
            reset(ch, w, intOps);
        }

        public void reset(NioChannel ch, NioSocketWrapper w, int intOps) {
            socket = ch;
            interestOps = intOps;
            socketWrapper = w;
        }

        public void reset() {
            reset(null, null, 0);
        }

        @Override
        public void run() {
            if (interestOps == OP_REGISTER) {
                try {
                    socket.getIOChannel().register(socket.getPoller().getSelector(), SelectionKey.OP_READ, socketWrapper);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                final SelectionKey key = socket.getIOChannel().keyFor(socket.getPoller().getSelector());
                if (key == null) {
                    // The key was cancelled (e.g. due to socket closure)
                    // and removed from the selector while it was being
                    // processed. Count down the connections at this point
                    // since it won't have been counted down when the socket
                    // closed.
//                    socket.socketWrapper.getEndpoint().countDownConnection();
                } else {
                    final NioSocketWrapper socketWrapper = (NioSocketWrapper) key.attachment();
                }
            }
        }
    }
    public NioSelectorPool getSelectorPool() {
        return selectorPool;
    }
    /**
     * Poller class.
     */
    public class Poller implements Runnable {
        private Selector selector;
        private volatile boolean close = false;
        private long nextExpiration = 0;//optimize expiration handling
        private final SynchronizedQueue<PollerEvent> events = new SynchronizedQueue<>();
        private AtomicLong wakeupCounter = new AtomicLong(0);

        private volatile int keyCount = 0;

        public Poller() throws IOException {
            this.selector = Selector.open();
        }
        /**
         * Registers a newly created socket with the poller.
         *
         * @param socket    The newly created socket
         */
        public void register(final NioChannel socket) {
            socket.setPoller(this);
            NioSocketWrapper ka = new NioSocketWrapper(socket, NioEndpoint.this);
            socket.setSocketWrapper(ka);
            ka.setPoller(this);
            PollerEvent r = eventCache.pop();
            ka.interestOps(SelectionKey.OP_READ);//this is what OP_REGISTER turns into.
            if ( r==null) r = new PollerEvent(socket,ka,OP_REGISTER);
            else r.reset(socket,ka,OP_REGISTER);
            addEvent(r);
//            ka.setReadTimeout(getConnectionTimeout());
//            ka.setWriteTimeout(getConnectionTimeout());
//            ka.setKeepAliveLeft(NioEndpoint.this.getMaxKeepAliveRequests());
//            ka.setSecure(isSSLEnabled());
        }
        private void addEvent(PollerEvent event) {
            events.offer(event);
            if ( wakeupCounter.incrementAndGet() == 0 ) selector.wakeup();
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
                    if (close){
                        events();
//                        timeout(0, false);
                        try {
                            selector.close();
                        } catch (IOException ioe) {
                            logger.error(("endpoint.nio.selectorCloseFail"), ioe);
                        }
                        break;

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
                    if (attachment == null) iterator.remove();
                    else {
                        iterator.remove();
                        processKey(sk, attachment);
                    }
                }

            }
//            state = Acceptor.AcceptorState.ENDED;
        }
        protected void reg(SelectionKey sk, NioSocketWrapper attachment, int intops) {
            sk.interestOps(intops);
            attachment.interestOps(intops);
        }
        protected void unreg(SelectionKey sk, NioSocketWrapper attachment, int readyOps) {
            //this is a must, so that we don't have multiple threads messing with the socket
            reg(sk,attachment,sk.interestOps()& (~readyOps));
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
            boolean result = false;
            PollerEvent pe = null;
            while ( (pe = events.poll()) != null ) {
                result = true;
                try {
                    pe.run();
                    pe.reset();
                    if (running && !paused) {
                        eventCache.push(pe);
                    }
                } catch ( Throwable x ) {
                    logger.error("",x);
                }
            }

            return result;
        }

        public int getKeyCount() {
            return keyCount;
        }

        public Selector getSelector() {
            return selector;
        }
    }

    public static class NioSocketWrapper extends SocketWrapperBase<NioChannel> {
        private final NioSelectorPool pool;

        private Poller poller = null;
        private int interestOps = 0;
        private CountDownLatch readLatch = null;
        private CountDownLatch writeLatch = null;
        private volatile SendfileData sendfileData = null;
        private volatile long lastRead = System.currentTimeMillis();
        private volatile long lastWrite = lastRead;

        public NioSocketWrapper(NioChannel channel, NioEndpoint endpoint) {
            super(channel, endpoint);
            pool = endpoint.getSelectorPool();
            socketBufferHandler = channel.getBufHandler();
        }

        public SendfileData getSendfileData() {
            return this.sendfileData;
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        public void setPoller(Poller poller) {
            this.poller = poller;
        }
        public int interestOps() { return interestOps;}
        public int interestOps(int ops) { this.interestOps  = ops; return ops; }
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
