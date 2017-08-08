package ru.otus_matveev_anton;

import java.io.StringWriter;

public interface MessageSystem {
    String MESSAGE_GENERATE_NEW_ID = "MESSAGE_GENERATE_NEW_ID";
    String MESSAGE_SIGN_IN_SUCCESS = "MESSAGE_SIGN_IN_SUCCESS";

    void sendMessage(Message message);

    int register(int clientId);
}
