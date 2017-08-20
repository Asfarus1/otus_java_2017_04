package ru.otus_matveev_anton.genaral;

public interface MessageReceiveListener {
    /**
     *
     * @param message - message :D
     * @return true if message processed
     */
    boolean onMessageReceive(Message message);
}
