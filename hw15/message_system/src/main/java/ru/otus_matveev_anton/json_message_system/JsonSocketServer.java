package ru.otus_matveev_anton.json_message_system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class JsonSocketServer implements MessageSystem {
    private final static Logger log = LogManager.getLogger(JsonSocketServer.class);

    private static final int BYTE_BUFFER_CAPACITY = 512;
    private static final int WORKERS_COUNT = 5;
    private static final int DEFAULT_OPER_DELAY_MS = 10;
    private final int port;

    private final Map<Address, Queue<String>> messages = new ConcurrentHashMap<>();
    private final Map<String, Set<Address>> groupAddresses = new ConcurrentHashMap<>();
    private final Map<Address, ChannelWrapper> addressReaders = new ConcurrentHashMap<>();
    private final Map<SocketChannel, ChannelWrapper> channelReaders = new ConcurrentHashMap<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(WORKERS_COUNT);

    public JsonSocketServer(int port) {
        this.port = port;
    }


    public void start() throws Exception {
        executor.submit(this::receiveMessage);
        executor.submit(this::sendMessage);
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
                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);
                            isOk = true;
                        } else if (key.isReadable()) {
                            channel = (SocketChannel) key.channel();
                            channelWrapper = channelReaders.get(channel);

                            if (channelWrapper == null) {
                                channelWrapper = new ChannelWrapper(channel);
                                channelReaders.put(channel, channelWrapper);
                                executor.submit(channelWrapper::registerChannel);
                            }

                            while ((read = channel.read(buffer)) > 0) {
                                buffer.flip();
                                channelWrapper.write(buffer.array(), read);
                                buffer.clear();
                            }

                            if (read == -1) {
                                log.info("Connection with {} closed", channel.getRemoteAddress());
                            } else if (read > 0){
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
    private void sendMessage() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_CAPACITY);
        while (true) {
            messages.forEach((Address a, Queue<String> q) -> {
                if (q != null && !q.isEmpty()) {
                    ChannelWrapper cw = addressReaders.get(a);
                    if (cw != null) {
                        try {
                            while (!q.isEmpty()) {
                                log.debug("send to address {} message {}", a, q.peek());
                                buffer.put(q.poll().getBytes());
                                buffer.put(JsonMessage.MESSAGE_SEPARATOR.getBytes());
                                buffer.flip();
                                while (buffer.hasRemaining()) {
                                    cw.channel.write(buffer);
                                }
                                buffer.clear();
                            }
                        } catch (IOException e) {
                            log.error(e);
                            buffer.clear();
                            cw.close();
                        }
                    }
                }
            });
            try {
                Thread.sleep(DEFAULT_OPER_DELAY_MS);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }

    private class ChannelWrapper implements Closeable {
        final PipedInputStream in;
        final PipedOutputStream out;
        final BufferedReader br;
        final SocketChannel channel;
        Address address;
        volatile boolean isRegistered;

        ChannelWrapper(SocketChannel channel) throws IOException {
            this.out = new PipedOutputStream();
            this.in = new PipedInputStream(this.out);
            this.channel = channel;
            this.br = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public void close(){
            if (address != null) {
                addressReaders.remove(address);
            }
            channelReaders.remove(channel);
            try {
                br.close();
            } catch (IOException e) {
                log.error(e);
            }
            try {
                channel.close();
            } catch (IOException e) {
                log.error(e);
            }
        }

        synchronized void write(byte[] buff, int len) throws IOException {
            out.write(buff, 0, len);
        }

        boolean hasMessage() throws IOException {
            return in.available() > 0;
        }

        String readTextMessage() throws IOException {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                synchronized (this) {
                    inputLine = br.readLine();
                }
                if(inputLine == null) break;

                stringBuilder.append(inputLine);
                if (inputLine.isEmpty() && !stringBuilder.toString().isEmpty()) {
                    log.debug("get message {}", stringBuilder);
                    return stringBuilder.toString();
                }
            }
            throw new IOException(channel.getRemoteAddress().toString() + " : socked input closed");
        }

        void registerChannel(){
            try {
                String json = readTextMessage();
                Message<String> message = new JsonMessage();
                message.loadFromPackagedData(json);

                Addressee addressee = message.getFrom();
                address = addressee.getAddress();

                String groupName = addressee.getGroupName();
                if (SpecialAddress.GENERATE_NEW.equals(address)) {
                    address = new ClientAddress();
                    log.info("For {} with groupName {} generated new address {}", channel.getRemoteAddress(), groupName, address);
                } else if (addressReaders.containsKey(address)) {
                    log.info("For {} with groupName {} address {} already taken", channel.getRemoteAddress(), groupName, address);
                    IOException e = new IOException("Address " + address.toString() + " already taken");
                    String msg = new JsonMessage(addressee, addressee, e).toPackedData();
                    channel.write(ByteBuffer.wrap(msg.concat(JsonMessage.MESSAGE_SEPARATOR).getBytes()));
                    throw e;
                } else {
                    log.info("For {} with groupName {} set address {}", channel.getRemoteAddress(), groupName, address);
                }

                addressee = new AddresseeImpl(address, groupName);

                messages.putIfAbsent(address, new LinkedBlockingQueue<>());
                Queue<String> queue = messages.get(address);
                queue.offer(new JsonMessage(addressee, addressee, MessageSystem.MESSAGE_OK).toPackedData());

                groupAddresses.putIfAbsent(groupName, ConcurrentHashMap.newKeySet());
                Set<Address> set = groupAddresses.get(groupName);
                set.add(address);

                addressReaders.put(address, this);
                isRegistered = true;
                executor.submit(this::receivingMessages);
            }catch (IOException e){
                log.error(e);
                close();
            }
        }

        private void receivingMessages() {
            if (!isRegistered) return;

            try {
                while (hasMessage()) {
                    String json = readTextMessage();
                    JsonMessage message = new JsonMessage();
                    try {
                        message.loadFromPackagedData(json);
                    } catch (MessageFormatException e) {
                        log.error("{} - unpacking message error: {}", channel.getRemoteAddress(), e.getMessage());
                        continue;
                    }

                    Addressee addressee = message.getTo();
                    Address address = addressee.getAddress();
                    if (address instanceof ClientAddress) {
                        messages.get(address).offer(json);
                    } else {
                        Set<Address> addresses = groupAddresses.get(addressee.getGroupName());
                        if (addresses!= null && addresses.size() > 0) {
                            if (SpecialAddress.ALL.equals(address)) {
                                addresses.forEach(a -> messages.get(a).offer(json));
                            } else if (SpecialAddress.ANYONE.equals(address)) {
                                addresses.stream().map(messages::get).min(Comparator.comparing(Queue::size)).ifPresent(q -> q.offer(json));
                            }
                        }
                    }
                    try {
                        Thread.sleep(DEFAULT_OPER_DELAY_MS);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }
            } catch (IOException e) {
                log.error(e);
                close();
            }
        }
    }
}

