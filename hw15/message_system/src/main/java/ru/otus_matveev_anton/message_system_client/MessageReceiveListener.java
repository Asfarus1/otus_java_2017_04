package ru.otus_matveev_anton.message_system_client;

import ru.otus_matveev_anton.Message;

public interface MessageReceiveListener {
    boolean onMessageReceive(Message message);
}
