package ru.otus_matveev_anton.genaral;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class MessageSystemClient<T> implements Closeable{
    private final List<MessageReceiveListener> listeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> shutdownRegistrations = new CopyOnWriteArrayList<>();

    private Addressee addressee;

    public void setAddressee(Addressee addressee) {
        this.addressee = addressee;
    }

    public Addressee getAddressee() {
        return addressee;
    }

    abstract public void sendMessage(Addressee to, Object data);

    abstract public void init();


    public void addMessageReceiveListener(MessageReceiveListener listener) {
        listeners.add(listener);
    }

    public boolean removeMessageReceiveListener(MessageReceiveListener listener) {
        return listeners.remove(listener);
    }

    public void addShutdownRegistration(Runnable runnable) {
        this.shutdownRegistrations.add(runnable);
    }

    protected void onMessageReceive(Message<T> message) {
        for (MessageReceiveListener listener : listeners) {
            if (listener.onMessageReceive(message)) {
                break;
            }
        }
    }

    @Override
    public void close() {
        for (Runnable runnable : shutdownRegistrations) {
            runnable.run();
        }
        shutdownRegistrations.clear();
    }
}
