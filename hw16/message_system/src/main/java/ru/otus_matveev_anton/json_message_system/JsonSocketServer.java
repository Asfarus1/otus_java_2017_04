package ru.otus_matveev_anton.json_message_system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
    private static final int REGISTRATION_TIMEOUT = 2000;
    private final int port;
    private final Lock registrationLock = new ReentrantLock();

    private final Map<String, List<Address>> groups = new ConcurrentHashMap<>();
    private final Map<Address, Queue<String>> messages = new ConcurrentHashMap<>();
    private final Map<Address, ChannelWrapper> addresseeWrappers = new ConcurrentHashMap<>();
    private final Map<SocketAddress, ChannelWrapper> socketAddressWrappers = new ConcurrentHashMap<>();

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
                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);
                            isOk = true;

                        } else if (key.isReadable()) {
                            channel = (SocketChannel) key.channel();
                            channelWrapper = socketAddressWrappers.get(channel.getRemoteAddress());

                            if (channelWrapper == null) {
                                channelWrapper = new ChannelWrapperImpl(channel, this::registerAddressee);
                                ChannelWrapper cw = channelWrapper;
                                channelWrapper.getClosingListener().addShutdownRegistration(()-> removeChanelWrapperFromMaps(cw));
                                socketAddressWrappers.put(channel.getRemoteAddress(), channelWrapper);
                            }

                            while((read = channel.read(buffer)) > 0) {
                                buffer.flip();
                                channelWrapper.write(buffer.array(), read);
                                buffer.clear();
                            }

                            if (read == -1) {
                                log.info("Connection with {} closed", channel.getRemoteAddress());
                            } else {
                                ChannelWrapper cw = channelWrapper;
                                executor.submit(()-> cw.receivingMessages(this::onMessageReceive));
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
        try {
            while (!executor.isShutdown()) {
                addresseeWrappers.forEach((a, cw) -> {
                            Queue<String> queue = messages.get(a);
                            if (queue != null && !queue.isEmpty()) {
                                String msg;
                                msg = queue.peek();
                                cw.sendMessage(msg);
                                queue.poll();
                                log.debug("send to address {} message {}", a, msg);
                            }
                        }
                );
                sleep();
            }
        } catch (Exception e) {
            log.error("sending message error", e);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(DEFAULT_OPER_DELAY_MS);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    private void addMessage(Address address, String msg) {
        Queue<String> queue = messages.putIfAbsent(address, new LinkedBlockingQueue<>());
        if (queue == null) {
            queue = messages.get(address);
        }
        queue.offer(msg);
        log.debug("add msg for {} : {}", address, msg);
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

    private Future<Addressee> registerAddressee(ChannelWrapper channelWrapper){
        return executor.submit(() -> {
            try {
                String json = channelWrapper.readTextMessage();
                Message<String> message = new JsonMessage();
                message.loadFromPackagedData(json);

                Addressee addressee = message.getFrom();
                final String groupName = addressee.getGroupName();
                Address address = addressee.getAddress();
                try {
                    if (registrationLock.tryLock(JsonSocketServer.REGISTRATION_TIMEOUT, TimeUnit.MILLISECONDS)) {
                        if (SpecialAddress.GENERATE_NEW.equals(address)) {
                            do {
                                addressee = new AddresseeImpl(new ClientAddress(), groupName);
                            } while (addresseeWrappers.containsKey(addressee.getAddress()));
                            log.info("generated new address {}", addressee);
                        } else if (address instanceof ClientAddress) {
                            if (addresseeWrappers.containsKey(address)) {
                                throw new IOException(String.format("Address %s already taken", addressee));
                            }
                        } else {
                            throw new IOException("Unacceptable address:" + address);
                        }

                        addresseeWrappers.put(addressee.getAddress(), channelWrapper);
                        socketAddressWrappers.put(channelWrapper.getChannel().getRemoteAddress(), channelWrapper);
                        groups.putIfAbsent(groupName, new CopyOnWriteArrayList<>());
                        groups.get(groupName).add(addressee.getAddress());

                        channelWrapper.sendMessage(new JsonMessage(addressee, addressee, JsonMessage.MESSAGE_OK).toPackedData());
                        return addressee;
                    }
                } finally {
                    registrationLock.unlock();
                }
            } catch (InterruptedException | IOException e) {
                channelWrapper.sendError(e);
            }
            return null;
        });
    }

    private void onMessageReceive(String json, ChannelWrapper channelWrapper) {
        if (json.isEmpty()) {
            log.info("got ping from {}", channelWrapper);
            return;
        }
        JsonMessage message = new JsonMessage();
        try {
            message.loadFromPackagedData(json);
            log.debug("unpack msg:{}", json);
        } catch (MessageFormatException e) {
            log.error("{} - unpacking message error: {}", channelWrapper, e.getMessage());
            return;
        }

        Addressee addressee = message.getTo();
        Address address = addressee.getAddress();
        if (address instanceof ClientAddress) {
            addMessage(address, json);
        } else {
            List<Address> addressList = groups.get(addressee.getGroupName());
            if (addressList != null) {
                if (SpecialAddress.ALL.equals(address)) {
                    addressList.forEach(a -> addMessage(a, json));
                } else if (SpecialAddress.ANYONE.equals(address)) {
                    addressList.stream()
                            .min(Comparator.comparing(a -> {
                                Queue<String> queue = messages.get(a);
                                return queue == null ? 0 : queue.size();
                            }))
                            .ifPresent(a -> addMessage(a, json));
                } else {
                    channelWrapper.sendError(new IllegalArgumentException("unknown recipient:" + addressee));
                }
            }
        }
    }

    private void removeChanelWrapperFromMaps(ChannelWrapper channelWrapper){
        try {
            registrationLock.lock();
            Addressee addressee = channelWrapper.getAddressee();
            if (addressee != null) {
                Address address = addressee.getAddress();
                addresseeWrappers.remove(address);
                List<Address> addressList = groups.get(addressee.getGroupName());
                if (addressList != null){
                    addressList.remove(address);
                }
            }
            try {
                socketAddressWrappers.remove(channelWrapper.getChannel().getRemoteAddress());
            } catch (IOException e) {
                log.error(e);
            }
        }finally {
            registrationLock.unlock();
        }
    }
}

