package ru.otus_matveev_anton.json_message_system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus_matveev_anton.genaral.Addressee;
import ru.otus_matveev_anton.genaral.AddresseeImpl;
import ru.otus_matveev_anton.genaral.ClosingListener;
import ru.otus_matveev_anton.genaral.SpecialAddress;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

class ChannelWrapperImpl implements ChannelWrapper {
    private static final Addressee MESSAGE_SERVER = new AddresseeImpl(SpecialAddress.MESSAGE_SERVER, "");
    private final static Logger log = LogManager.getLogger(ChannelWrapperImpl.class);

    private final PipedInputStream in;
    private final PipedOutputStream out;
    private final BufferedReader br;
    private final SocketChannel channel;
    private final Future<Addressee> fAddressee;
    private Lock lock = new ReentrantLock();
    private ClosingListener closingListener = new ClosingListener();

    ChannelWrapperImpl(SocketChannel channel, Function<ChannelWrapper,Future<Addressee>> registrar) throws IOException {
        this.out = new PipedOutputStream();
        this.in = new PipedInputStream(this.out);
        this.channel = channel;
        this.br = new BufferedReader(new InputStreamReader(in));
        fAddressee = registrar.apply(this);
    }

    @Override
    public void close() {
        closingListener.onClose();
        try {
            channel.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    @Override
    public void write(byte[] buff, int len) throws IOException {
        out.write(buff, 0, len);
    }

    private boolean hasMessage() throws IOException {
        return in.available() > 0;
    }

    @Override
    public String readTextMessage(){
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        boolean wasAnyRead = false;
        try {
            while (channel.isOpen() && (inputLine = br.readLine()) != null) {
                stringBuilder.append(inputLine);
                if (inputLine.isEmpty() && wasAnyRead) {
                    return stringBuilder.toString();
                }
                wasAnyRead = true;
            }
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void sendError(Throwable throwable) {
        sendMessage(new JsonMessage(MESSAGE_SERVER, MESSAGE_SERVER , throwable).toPackedData());
    }

    @Override
    public Addressee getAddressee() {
        try {
            return fAddressee.get();
        } catch (InterruptedException|ExecutionException e) {
            log.error(e);
        }
        return null;
    }

    @Override
    public void sendMessage(String msg) {
        ByteBuffer bf;
        if (msg != null) {
            byte[] bytes = msg.getBytes();
            bf = ByteBuffer.allocate(bytes.length + JsonMessage.MESSAGE_SEPARATOR.length);
            bf.put(bytes);
        } else {
            bf = ByteBuffer.allocate(JsonMessage.MESSAGE_SEPARATOR.length);
        }
        bf.put(JsonMessage.MESSAGE_SEPARATOR);
        bf.flip();
        try {
            channel.write(bf);
        } catch (IOException e) {
            log.error(e);
        }
    }

    @Override
    public void receivingMessages(BiConsumer<String,ChannelWrapper> textMessageReceiveListener) {
        try {
            fAddressee.get();
            if (lock.tryLock()) {
                while (hasMessage()) {
                    String json = readTextMessage();
                    textMessageReceiveListener.accept(json, this);
                }
            }
        } catch (Exception e) {
            log.error("receiving messages error", e);
            close();
        } finally {
            lock.unlock();
        }
    }

    public ClosingListener getClosingListener() {
        return closingListener;
    }
}
