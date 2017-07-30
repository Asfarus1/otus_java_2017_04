package ru.otus_matveev_anton.app;


import ru.otus_matveev_anton.messageSystem.Address;
import ru.otus_matveev_anton.messageSystem.MessageSystem;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MessageSystemContext {
    private final MessageSystem messageSystem;

    private Set<Address> frontAddresses;
    private Set<Address> dbAddresses;

    public MessageSystemContext(MessageSystem messageSystem) {
        this.messageSystem = messageSystem;
        frontAddresses = new CopyOnWriteArraySet<>();
        dbAddresses = new CopyOnWriteArraySet<>();
    }

    public MessageSystem getMessageSystem() {
        return messageSystem;
    }

    public Set<Address> getFrontAddresses() {
        return Collections.unmodifiableSet(frontAddresses);
    }

    public void addFrontAddresses(Address frontAddress) {
        this.frontAddresses.add(frontAddress);
    }

    public Set<Address> getDbAddresses() {
        return Collections.unmodifiableSet(dbAddresses);
    }

    public void addDbAddress(Address dbAddress) {
        this.dbAddresses.add(dbAddress);
    }
}
