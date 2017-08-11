package ru.otus_matveev_anton.genaral;

import java.util.Objects;

public abstract class Message<T> {
    protected Addressee from;

    protected Addressee to;

    protected Object data;

    public Message() {
    }

    public Message(Addressee from, Addressee to, Object data) {
        Objects.requireNonNull(from, "from must mot be null");
        Objects.requireNonNull(to, "to must mot be null");
        this.from = from;
        this.to = to;
        this.data = data;
    }

    abstract public T toPackedData();

    abstract public void loadFromPackagedData(T packagedData) throws MessageFormatException;

    public Addressee getFrom() {
        return from;
    }

    public Addressee getTo() {
        return to;
    }

    public Object getData() {
        return data;
    }
}
