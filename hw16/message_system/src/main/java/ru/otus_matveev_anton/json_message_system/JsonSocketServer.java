package ru.otus_matveev_anton.json_message_system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JsonSocketServer implements MessageSystem, JsonSocketServerMBean {
    private final static Logger log = LogManager.getLogger(JsonSocketServer.class);

    private static final int BYTE_BUFFER_CAPACITY = 512;
    private static final int WORKERS_COUNT = 5;
    private static final int DEFAULT_OPER_DELAY_MS = 10;
    private static final int PING_TIMEOUT = 5_000;
    private static final int SO_TIMEOUT = 20_000;
    private static final int REGISTRATION_TIMEOUT = 2000;
    private final int port;
    private final Lock registrationLock = new ReentrantLock();

    private final Map<Addressee, ChannelWrapper> addresseeWrappers = new ConcurrentHashMap<>();
    private final Map<SocketChannel, ChannelWrapper> channelWrappers = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(WORKERS_COUNT);

    public JsonSocketServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("ru.otus_matveev_anton.json_message_system:type=JsonSocketServer");
        mbs.registerMBean(this, name);
        executor.submit(this::receiveMessage);
        executor.submit(this::sendingMessage);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void receiveMessage() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress("localhost", port));

            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);

            log.info("Server started on port: {}", port);

            SocketChannel channel;
            ChannelWrapper channelWrapper;
            ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_CAPACITY);
            boolean isOk;
            int read;

            while (!executor.isShutdown()) {
                selector.select();

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    channelWrapper = null;
                    isOk = false;
                    try {
                        if (key.isAcceptable()) {
                            channel = serverSocketChannel.accept();
                            log.info("Accepted connection from {}", channel.getRemoteAddress());
                            channel.socket().setSoTimeout(SO_TIMEOUT);
                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);
                            isOk = true;

                        } else if (key.isReadable()) {
                            channel = (SocketChannel) key.channel();
                            channelWrapper = channelWrappers.get(channel);

                            if (channelWrapper == null) {
                                channelWrapper = new ChannelWrapper(channel);
                                channelWrappers.put(channel, channelWrapper);
                                executor.submit(channelWrapper::registerChannel);
                            }

                            /*while*/
                            if ((read = channel.read(buffer)) > 0) {
                                buffer.flip();
                                channelWrapper.write(buffer.array(), read);
                                buffer.clear();
                            }

                            if (read == -1) {
                                log.info("Connection with {} closed", channel.getRemoteAddress());
                            } else {
                                if (!channelWrapper.isBusy) {
                                    executor.submit(channelWrapper::receivingMessages);
                                }
                                isOk = true;
                            }
                        }
                    } catch (IOException e) {
                        log.error(e);
                    } finally {
                        iterator.remove();
                        if (!isOk) {
                            key.cancel();
                            if (channelWrapper != null) {
                                channelWrapper.close();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("ServerSocketChannel.open()", e);
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void sendingMessage() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_CAPACITY);
        try {
            while (!executor.isShutdown()) {
                addresseeWrappers.forEach((a, cw) -> {
                            Queue<String> queue = cw.queue;
                            try {
                                if (queue != null && !queue.isEmpty()) {
                                    String msg;
                                    while (!queue.isEmpty()) {
                                        msg = queue.peek();
                                        send(buffer, cw, msg);
                                        queue.poll();
                                        log.debug("send to address {} message {}", a, msg);
                                    }
                                } else if (cw.lastActive + PING_TIMEOUT < System.currentTimeMillis()) {
                                    send(buffer, cw, null);
                                    log.debug("send ping to address {}", a);
                                }
                            } catch (IOException e) {
                                log.error("sending message error", e);
                                cw.close();
                            }
                        }
                );
                sleep();
            }
        } catch (Exception e) {
            log.error("sending message error", e);
        }
    }

    private void send(ByteBuffer buffer, ChannelWrapper cw, String msg) throws IOException {
        try {
            if (msg != null) {
                byte[] bytes = msg.getBytes();
                int left = bytes.length;
                while (left > 0){
                        buffer.put(bytes, bytes.length - left, left > buffer.limit() ? buffer.limit() : left);
                        left -= buffer.limit();
                    buffer.flip();
                    cw.channel.write(buffer);
                    buffer.clear();
                }
            }
            buffer.put(JsonMessage.MESSAGE_SEPARATOR);
            buffer.flip();
            while (buffer.hasRemaining()) {
                cw.channel.write(buffer);
            }
            cw.active();
        } finally {
            buffer.clear();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(DEFAULT_OPER_DELAY_MS);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    private class ChannelWrapper implements Closeable {
        final PipedInputStream in;
        final PipedOutputStream out;
        final BufferedReader br;
        final SocketChannel channel;
        Addressee addressee;
        volatile boolean isBusy = true;
        volatile long lastActive;
        Queue<String> queue;
        Lock lock = new ReentrantLock();

        ChannelWrapper(SocketChannel channel) throws IOException {
            this.out = new PipedOutputStream();
            this.in = new PipedInputStream(this.out);
            this.channel = channel;
            this.br = new BufferedReader(new InputStreamReader(in));
            queue = new ConcurrentLinkedQueue<>();
        }

        @Override
        public void close() {
            if (addressee != null) {
                addresseeWrappers.remove(addressee);
            }
            channelWrappers.remove(channel);
            try {
                channel.close();
            } catch (IOException e) {
                log.error("ChannelWrapper close", e);
            }
        }

        void write(byte[] buff, int len) throws IOException {
            out.write(buff, 0, len);
        }

        void active() {
            lastActive = System.currentTimeMillis();
        }

        boolean hasMessage() throws IOException {
            return in.available() > 0;
        }

        String readTextMessage() throws IOException {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            boolean isAnyRead = false;
            while (channel.isOpen() && (inputLine = br.readLine()) != null) {
                stringBuilder.append(inputLine);
                if (inputLine.isEmpty() && isAnyRead) {
                    return stringBuilder.toString();
                }
                isAnyRead = true;
            }
            throw new IOException(channel.getRemoteAddress().toString() + " : socked input closed");
        }

        void registerChannel() {
            try {
                String json = readTextMessage();
                Message<String> message = new JsonMessage();
                message.loadFromPackagedData(json);

                Addressee addressee = message.getFrom();
                String groupName = addressee.getGroupName();

                try {
                    if (registrationLock.tryLock(REGISTRATION_TIMEOUT, TimeUnit.MILLISECONDS)) {
                        if (SpecialAddress.GENERATE_NEW.equals(addressee.getAddress())) {
                            do {
                                addressee = new AddresseeImpl(new ClientAddress(), groupName);
                            } while (addresseeWrappers.containsKey(addressee));
                            log.info("For {} generated new address {}", channel.getRemoteAddress(), addressee);
                        } else if (addresseeWrappers.containsKey(addressee)) {
                            sendError(addressee, String.format("Address %s already taken", addressee));
                        } else {
                            log.info("For {} set address {}", channel.getRemoteAddress(), addressee);
                        }
                        this.addressee = addressee;
                        addresseeWrappers.put(this.addressee, this);
                    }
                } catch (InterruptedException e) {
                    sendError(addressee, "registration failure " + e.getLocalizedMessage());
                } finally {
                    registrationLock.unlock();
                }
                queue.offer(new JsonMessage(addressee, addressee, JsonMessage.MESSAGE_OK).toPackedData());
            } catch (IOException e) {
                log.error("register channel error", e);
                close();
                return;
            } finally {
                isBusy = false;
            }
            executor.submit(this::receivingMessages);
        }

        private void sendError(Addressee addressee, String msg) throws IOException {
            IOException e = new IOException(msg);
            byte[] bytes = new JsonMessage(addressee, addressee, e).toPackedData().getBytes();
            ByteBuffer bf = ByteBuffer.allocate(bytes.length + JsonMessage.MESSAGE_SEPARATOR.length);
            bf.put(bytes);
            bf.put(JsonMessage.MESSAGE_SEPARATOR);
            bf.flip();
            channel.write(bf);
            throw e;
        }

        private void receivingMessages() {
            if (isBusy) return;

            try {
                if (lock.tryLock()) {
                    isBusy = true;
                    while (hasMessage()) {
                        String json = readTextMessage();
                        if (json.isEmpty()) {
                            log.info("got ping from {}", addressee);
                            continue;
                        }
                        JsonMessage message = new JsonMessage();
                        try {
                            message.loadFromPackagedData(json);
                            log.debug("unpack msg:{}", json);
                        } catch (MessageFormatException e) {
                            log.error("{} - unpacking message error: {}", channel.getRemoteAddress(), e.getMessage());
                            continue;
                        }

                        Addressee addressee = message.getTo();
                        Address address = addressee.getAddress();
                        if (address instanceof ClientAddress) {
                            addMessage(addressee, json);
                        } else {
                            final String groupName = addressee.getGroupName();
                            if (SpecialAddress.ALL.equals(address)) {
                                addresseeWrappers.keySet().stream()
                                        .filter(a -> Objects.equals(a.getGroupName(), groupName))
                                        .forEach(a -> addMessage(a, json));
                            } else if (SpecialAddress.ANYONE.equals(address)) {
                                addresseeWrappers.values().stream()
                                        .filter(cw -> cw.queue != null && groupName.equals(cw.addressee.getGroupName()))
                                        .min(Comparator.comparing(cw -> queue.size()))
                                        .ifPresent(cw -> addMessage(cw.addressee, json));
                            } else {
                                sendError(this.addressee, "unknown recipient " + addressee);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("receiving messages error", e);
                close();
            } finally {
                isBusy = false;
                lock.unlock();
            }
        }
    }

    private void addMessage(Addressee addressee, String msg) {
        ChannelWrapper channelWrapper = addresseeWrappers.get(addressee);
        if (channelWrapper == null || channelWrapper.queue == null) {
            log.warn("failed add msg for {}, address unknown : {}", addressee, msg);
        } else {
            log.debug("add msg for {} : {}", addressee, msg);
            channelWrapper.queue.add(msg);
        }
    }

    @Override
    public boolean getRunning() {
        return true;
    }

    @Override
    public void setRunning(boolean running) {
        if (!running) {
            executor.shutdown();
            log.info("Bye.");
        }
    }
}

