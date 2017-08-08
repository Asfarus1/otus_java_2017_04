package ru.otus_matveev_anton.message_system_client;

import ru.otus_matveev_anton.Message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class MessageSystemClient<T> {
    protected final List<MessageReceiveListener> listeners = new CopyOnWriteArrayList<>();
    protected final List<Runnable> shutdownRegistrations = new CopyOnWriteArrayList<>();

    abstract public boolean sendMessage(Message<T> message);

    abstract protected void register() throws IOException;

    abstract protected void signIn(String clientId) throws IOException;

    abstract public void init();

    public void addMessageReceiveListener(MessageReceiveListener listener){
        listeners.add(listener);
    }

    public boolean  removeMessageReceiveListener(MessageReceiveListener listener){
     return listeners.remove(listener);
    }

    public void addShutdownRegistration(Runnable runnable) {
        this.shutdownRegistrations.add(runnable);
    }
}
