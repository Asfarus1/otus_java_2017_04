package ru.otus_matveev_anton.json_message_system;

import ru.otus_matveev_anton.genaral.Addressee;
import ru.otus_matveev_anton.genaral.ClosingListener;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.function.BiConsumer;

public interface ChannelWrapper extends Closeable{
    String readTextMessage();

    SocketChannel getChannel();

    void write(byte[] buff, int len) throws IOException;

    void sendError(Throwable msg);

    Addressee getAddressee();//blocking

    void sendMessage(String msg);

    void receivingMessages(BiConsumer<String,ChannelWrapper> textMessageReceiveListener);

    ClosingListener getClosingListener();

    void close();
}
