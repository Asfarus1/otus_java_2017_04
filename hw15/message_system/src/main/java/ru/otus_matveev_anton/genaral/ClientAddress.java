package ru.otus_matveev_anton.genaral;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientAddress implements Address {
    private final int id;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    public ClientAddress(int id) {
        this.id = id;
    }

    public ClientAddress() {
        id = ID_GENERATOR.incrementAndGet();
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientAddress that = (ClientAddress) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}