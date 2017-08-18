package ru.otus_matveev_anton.json_message_system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JsonSocketServer implements MessageSystem {
    private final static Logger log = LogManager.getLogger(JsonSocketServer.class);

    private static final int BYTE_BUFFER_CAPACITY = 512;
    private static final int WORKERS_COUNT = 5;
    private static final int DEFAULT_OPER_DELAY_MS = 10;
    private final int port;

    private final Map<Address, Queue<String>> messages = new ConcurrentHashMap<>();
    private final Map<String, Set<Address>> groupAddresses = new ConcurrentHashMap<>();
    private final Map<Address, ChannelWrapper> addressWrappers = new ConcurrentHashMap<>();
    private final Map<SocketChannel, ChannelWrapper> channelWrappers = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(WORKERS_COUNT);

    public JsonSocketServer(int port) {
        this.port = port;
    }


    public void start() throws Exception {
        executor.submit(this::receiveMessage);
        executor.submit(this::sendingMessage);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void receiveMessage(){
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

            while (true) {
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
                            channel.socket().setSoTimeout(1);
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

                            while ((read = channel.read(buffer)) > 0) {
                                buffer.flip();
                                channelWrapper.write(buffer.array(), read);
                                buffer.clear();
                            }

                            if (read == -1) {
                                log.info("Connection with {} closed", channel.getRemoteAddress());
                            }else {
                                executor.submit(channelWrapper::receivingMessages);
                                isOk = true;
                            }
                        }
                    } catch (IOException e) {
                        log.error(e);
                    } finally {
                        if (!isOk){
                            key.cancel();
                            if (channelWrapper != null){
                                channelWrapper.close();
                            }
                        }
                        iterator.remove();
                    }
                }
            }
        }catch (IOException e){
            log.error(e);
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void sendingMessage() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_CAPACITY);
        while (true) {
            messages.forEach((Address a, Queue<String> q) -> {
                if (q != null && !q.isEmpty()) {
                    ChannelWrapper cw = addressWrappers.get(a);
                    String msg;
                    if (cw != null) {
                        try {
                            while (!q.isEmpty()) {
                                msg = q.peek();
                                buffer.put(msg.getBytes());
                                send(buffer, cw.channel);
                                q.poll();
                                cw.active();
                                log.debug("send to address {} message {}", a, msg);
                            }
                            if (cw.lastActive + 5_000 < System.currentTimeMillis()){
                                send(buffer, cw.channel);
                            }
                        } catch (IOException e) {
                            log.error(e);
                            buffer.clear();
                            cw.close();
                        }
                    }
                }
            });
            sleep();
        }
    }

    private void send(ByteBuffer buffer, SocketChannel channel) throws IOException {
        buffer.put(JsonMessage.MESSAGE_SEPARATOR);
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();
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
        Address address;
        volatile boolean isRegistered;
        volatile long lastActive;
        Lock lock = new ReentrantLock();

        ChannelWrapper(SocketChannel channel) throws IOException {
            this.out = new PipedOutputStream();
            this.in = new PipedInputStream(this.out);
            this.channel = channel;
            this.br = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public void close(){
            if (address != null) {
                addressWrappers.remove(address);
            }
            channelWrappers.remove(channel);
            try {
                channel.close();
            } catch (IOException e) {
                log.error(e);
            }
        }

        void write(byte[] buff, int len) throws IOException {
            out.write(buff, 0, len);
            active();
        }

        void active(){
            lastActive = System.currentTimeMillis();
        }

        boolean hasMessage() throws IOException {
            return in.available() > 0;
        }

        String readTextMessage() throws IOException {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while (channel.isOpen()) {
                if (br.ready()) {
                    if ((inputLine = br.readLine()) == null) break;

                    stringBuilder.append(inputLine);
                    if (inputLine.isEmpty() && !stringBuilder.toString().isEmpty()) {
                        log.debug("get message {}", stringBuilder);
                        return stringBuilder.toString();
                    }
                } else {
                    sleep();
                }
            }
            throw new IOException(channel.getRemoteAddress().toString() + " : socked input closed");
        }

        void registerChannel(){

            try {
                if (lock.tryLock()) {
                    String json = readTextMessage();
                    Message<String> message = new JsonMessage();
                    message.loadFromPackagedData(json);

                    Addressee addressee = message.getFrom();
                    address = addressee.getAddress();

                    String groupName = addressee.getGroupName();
                    if (SpecialAddress.GENERATE_NEW.equals(address)) {
                        address = new ClientAddress();
                        log.info("For {} with groupName {} generated new address {}", channel.getRemoteAddress(), groupName, address);
                    } else if (addressWrappers.containsKey(address)) {
                        addressTakenError(addressee);
                    } else {
                        try {
                            ClientAddress.registeredAddressId(((ClientAddress)address).getId());
                        }catch (RuntimeException re){
                           addressTakenError(addressee);
                        }
                    }

                    addressee = new AddresseeImpl(address, groupName);

                    messages.putIfAbsent(address, new LinkedBlockingQueue<>());
                    Queue<String> queue = messages.get(address);
                    queue.offer(new JsonMessage(addressee, addressee, MessageSystem.MESSAGE_OK).toPackedData());

                    groupAddresses.putIfAbsent(groupName, ConcurrentHashMap.newKeySet());
                    Set<Address> set = groupAddresses.get(groupName);
                    set.add(address);

                    addressWrappers.put(address, this);
                    isRegistered = true;
                    executor.submit(this::receivingMessages);
                }
            }catch (IOException e){
                log.error(e);
                close();
            }finally {
                lock.unlock();
            }
        }

        private void addressTakenError(Addressee addressee) throws IOException {
            IOException e = new IOException("Address " + address.toString() + " already taken");
            byte[] msg = new JsonMessage(addressee, addressee, e).toPackedData().getBytes();
            ByteBuffer bf = ByteBuffer.allocate(msg.length + JsonMessage.MESSAGE_SEPARATOR.length);
            bf.put(msg);
            bf.put(JsonMessage.MESSAGE_SEPARATOR);
            bf.flip();
            channel.write(bf);
            address = null;
            log.error("For {} with groupName {} address {} already taken", channel.getRemoteAddress(), addressee.getGroupName(), addressee.getAddress());
            throw e;
        }

        private void receivingMessages() {
            if (!isRegistered) return;

            try {
                if (lock.tryLock()) {
                    while (hasMessage()) {
                        String json = readTextMessage();
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
                            addMessage(address, json);
                        } else {
                            Set<Address> addresses = groupAddresses.get(addressee.getGroupName());
                            if (addresses != null && addresses.size() > 0) {
                                if (SpecialAddress.ALL.equals(address)) {
                                    addresses.forEach(a->addMessage(a, json));
                                } else if (SpecialAddress.ANYONE.equals(address)) {
                                    addresses.stream()
                                            .min(Comparator.comparing(a->{
                                                Queue<String> q = messages.get(a);
                                                return q == null ? 0 : q.size();
                                            }))
                                            .ifPresent(a->addMessage(a, json));
                                }
                            }
                        }
                        sleep();
                    }
                }
            } catch (IOException e) {
                log.error(e);
                close();
            }finally {
                lock.unlock();
            }
        }

        private void addMessage(Address address, String msg){
            Queue<String> queue = messages.get(address);
            if (queue == null){
                log.warn("failed add msg for {}, address unknown : {}", address, msg);
            }else {
                log.debug("add msg for {} : {}", address, msg);
                queue.add(msg);
            }
        }
    }
}

