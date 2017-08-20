package ru.otus_matveev_anton.genaral;

import java.util.UUID;

public class ClientAddress implements Address {
    private final String id;

    ClientAddress(String id) {
        this.id = id;
    }

    public ClientAddress() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
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

        return id == null ? that.id == null : id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
