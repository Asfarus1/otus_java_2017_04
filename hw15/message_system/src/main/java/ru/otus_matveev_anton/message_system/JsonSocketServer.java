package ru.otus_matveev_anton.message_system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.Address;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class JsonSocketServer {
    private final static Logger log = LogManager.getLogger(JsonSocketServer.class);
    private final int port;
    private final Map<Address, Queue> messages = new ConcurrentHashMap<>();
    private final Map<String, Address> groupAddresses = new ConcurrentHashMap<>();
    private final Map<Address, Channel> addressChannels = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public JsonSocketServer(int port) {
        this.port = port;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void start() throws Exception {
        executor.submit(this::mirror);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress("localhost", port));

            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);

            log.info("Started on port: " + port);

            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
//                    try {
//                        if (key.isAcceptable()) {
//                            SocketChannel channel = serverSocketChannel.accept(); //non blocking accept
//                            String remoteAddress = channel.getRemoteAddress().toString();
//
//                            channel.configureBlocking(false);
//                            channel.register(selector, SelectionKey.OP_READ);
//
//                            channelMessages.put(remoteAddress, new ChannelMessages(channel));
//
//                        } else if (key.isReadable()) {
//                            SocketChannel channel = (SocketChannel) key.channel();
//
//                            ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
//                            int read = channel.read(buffer);
//                            if (read != -1) {
//                                String result = new String(buffer.array()).trim();
//                                System.out.println("Message received: " + result + " from: " + channel.getRemoteAddress());
//                                channelMessages.get(channel.getRemoteAddress().toString()).messages.add(result);
//                            } else {
//                                key.cancel();
//                                String remoteAddress = channel.getRemoteAddress().toString();
//                                channelMessages.remove(remoteAddress);
//                                System.out.println("Connection closed, key canceled");
//                            }
//                        }
//                    } catch (IOException e) {
//                        logger.log(Level.SEVERE, e.getMessage());
//                    } finally {
//                        iterator.remove();
//                    }
                }
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private Object mirror() throws InterruptedException {
        while (true) {
//            for (Map.Entry<String, ChannelMessages> entry : channelMessages.entrySet()) {
//                ChannelMessages channelMessages = entry.getValue();
//                if (channelMessages.channel.isConnected()) {
//                    channelMessages.messages.forEach(message -> {
//                        try {
//                            System.out.println("Mirroring message to: " + entry.getKey());
//                            ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
//                            buffer.put(message.getBytes());
//                            buffer.put(MESSAGES_SEPARATOR.getBytes());
//                            buffer.flip();
//                            while (buffer.hasRemaining()) {
//                                channelMessages.channel.write(buffer);
//                            }
//                        } catch (IOException e) {
//                            logger.log(Level.SEVERE, e.getMessage());
//                        }
//                    });
//                    channelMessages.messages.clear();
//                }
//            }
//            Thread.sleep(MIRROR_DELAY);
        }
    }
}

