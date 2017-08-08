package ru.otus_matveev_anton.addressing;

import ru.otus_matveev_anton.Address;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientAddress implements Address {
    private final int id;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    public ClientAddress(int id) {
        this.id = id;
    }

    public ClientAddress() {
        id = ID_GENERATOR.getAndIncrement();
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
