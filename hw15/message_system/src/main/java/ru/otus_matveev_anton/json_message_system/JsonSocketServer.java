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
    public static final String MESSAGE_SEPARATOR = "\n\n";
    private final int port;
    private final Map<Address, Queue<String>> messages = new ConcurrentHashMap<>();
    private final Map<String, Set<Address>> groupAddresses = new ConcurrentHashMap<>();
    private final Map<Channel, Address> addressChannels = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(WORKERS_COUNT);

    public JsonSocketServer(int port) {
        this.port = port;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void start() throws Exception {

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress("localhost", port));

            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);

            log.info("Server started on port: {}", port);
            HashMap<Channel, ChannelReader> newChannels = new HashMap<>();

            ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_CAPACITY);
            int read;
            boolean isProcessed;
            while (true) {
                selector.select();

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    isProcessed = false;
                    try {
                        if (key.isAcceptable()) {
                            SocketChannel channel = serverSocketChannel.accept(); //non blocking accept
                            log.info("Accepted connection from {}", channel.getRemoteAddress());
                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);
                            isProcessed = true;
                        }

                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            ChannelReader channelReader = newChannels.get(channel);
                            boolean isFirstRead = channelReader == null;
                            if (isFirstRead) {
                                channelReader = new ChannelReader(channel);
                                newChannels.put(channel, channelReader);
                            }

                            boolean isReading = false;
                            while ((read = channel.read(buffer)) > 0) {
                                buffer.flip();
                                channelReader.write(buffer.array(), read);
                                buffer.clear();
                                isReading = true;
                            }

                            if (read == -1) {
                                key.cancel();
                                newChannels.remove(channel);
                                log.info("Connection with {} closed", channel.getRemoteAddress());
                            } else {
                                if (isReading) {
                                    if (isFirstRead) {
                                        channelReader.registerChannel(selector);
                                    }
                                    if (!channelReader.isActive) {
                                        executor.submit(channelReader::receivingMessages);
                                    }
                                }
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                        }
//                        if (key.isConnectable()){
//                            SocketChannel channel = (SocketChannel) key.channel();
//                            log.info("connect with {}", channel.getRemoteAddress());
//                            boolean success = false;
//                            try {
//                                success = channel.finishConnect();
//                            } catch (IOException e) {
//                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                            }
//                            if (!success) {
//                                // An error occurred; handle it
//
//                                // Unregister the channel with this selector
//                                key.cancel();
//                            }
//                            int ops = key.interestOps();
//
//                            if((ops & SelectionKey.OP_WRITE) != 0)
//                            {
//                                key.interestOps(SelectionKey.OP_WRITE);
//                            }
//                            else
//                            {
//                                key.interestOps(SelectionKey.OP_READ);
//                            }
//                        }

                        if (key.isWritable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            Address address = addressChannels.get(channel);
                            if (address != null) {
                                Queue<String> queue = messages.get(address);
                                if (queue != null) {
                                    while (!queue.isEmpty()) {
                                        log.debug("send to address {} message {}", address, queue.peek());
                                        buffer.put(queue.poll().getBytes());
                                        buffer.put(MESSAGE_SEPARATOR.getBytes());
                                        buffer.flip();
                                        channel.write(buffer);
                                        if (buffer.hasRemaining()){
                                            buffer.compact();
                                        }else {
                                            buffer.clear();
                                        }
                                    }
                                }
                            }
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } catch (IOException e) {
                        log.error(e);
                        newChannels.remove(key.channel());
                        addressChannels.remove(key.channel());
                        key.cancel();
                    } finally {
                        if (isProcessed)
                            iterator.remove();
                    }
                }
            }
        }
    }

    private class ChannelReader implements AutoCloseable {
        final PipedInputStream in;
        final PipedOutputStream out;
        final BufferedReader br;
        final SocketChannel channel;
        volatile boolean isActive = false;

        ChannelReader(SocketChannel channel) throws IOException {
            this.out = new PipedOutputStream();
            this.in = new PipedInputStream(this.out);
            this.channel = channel;
            this.br = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public void close() throws Exception {
            br.close();
        }

        void write(byte[] buff, int len) throws IOException {
            out.write(buff, 0, len);
        }

        boolean hasMessage() throws IOException {
            return in.available() > 0;
        }

        String readTextMessage() throws IOException {
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                stringBuilder.append(inputLine);
                if (inputLine.isEmpty() && !stringBuilder.toString().isEmpty()) {
                    log.debug("get message {}", stringBuilder);
                    return stringBuilder.toString();
                }
            }
            throw new IOException(channel.getRemoteAddress().toString() + " : socked input closed");
        }

        void registerChannel(Selector selector) throws IOException {
            String json = readTextMessage();
            Message<String> message = new JsonMessage();
            message.loadFromPackagedData(json);

            Addressee addressee = message.getFrom();
            Address address = addressee.getAddress();

            String groupName = addressee.getGroupName();
            if (SpecialAddress.GENERATE_NEW.equals(address)) {
                address = new ClientAddress();
                log.info("For {} with groupName {} generated new address {}", channel.getRemoteAddress(), groupName, address);
            } else if (addressChannels.containsValue(address)) {
                log.info("For {} with groupName {} address {} already taken", channel.getRemoteAddress(), groupName, address);
                String msg = new JsonMessage(addressee, addressee, "Address " + address.toString() + " already taken").toPackedData();
                channel.write(ByteBuffer.wrap(msg.concat("\n\n").getBytes()));
                throw new IOException(msg);
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

            addressChannels.put(channel, address);
            channel.register(selector, SelectionKey.OP_WRITE);
        }

        private void receivingMessages() {
            try {
                isActive = true;
                while (hasMessage()) {
                    String json = readTextMessage();
                    JsonMessage message = new JsonMessage();
                    try {
                        message.loadFromPackagedData(json);
                    } catch (MessageFormatException e) {
                        log.error("{} - unpacking message {} error: {}", channel.getRemoteAddress(), json, e.getMessage());
                        continue;
                    }

                    Addressee addressee = message.getTo();
                    Address address = addressee.getAddress();
                    if (address instanceof ClientAddress) {
                        messages.get(address).offer(json);
                    } else {
                        Set<Address> addresses = groupAddresses.get(addressee.getGroupName());
                        if (addresses.size() > 0) {
                            if (SpecialAddress.ALL.equals(address)) {
                                addresses.forEach(a -> messages.get(a).offer(json));
                            } else if (SpecialAddress.ANYONE.equals(address)) {
                                addresses.stream().map(messages::get).min(Comparator.comparing(Queue::size)).ifPresent(q -> q.offer(json));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error(e);
            } finally {
                isActive = false;
            }
        }
    }
}

