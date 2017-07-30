package ru.otus_matveev_anton.ms.front;


import ru.otus_matveev_anton.app.FrontendService;
import ru.otus_matveev_anton.app.MessageSystemContext;
import ru.otus_matveev_anton.messageSystem.Address;
import ru.otus_matveev_anton.messageSystem.Addressee;
import ru.otus_matveev_anton.messageSystem.Message;
import ru.otus_matveev_anton.ms.db.MsgGetUserId;

import java.util.HashMap;
import java.util.Map;

public class FrontendServiceImpl implements FrontendService, Addressee {
    private final Address address;
    private final MessageSystemContext context;

    private final Map<Integer, String> users = new HashMap<>();

    public FrontendServiceImpl(MessageSystemContext context, Address address) {
        this.context = context;
        this.address = address;
    }

    public void init() {
        context.getMessageSystem().addAddressee(this);
    }

    @Override
    public Address getAddress() {
        return address;
    }

    public void handleRequest(String login) {
        Message message = new MsgGetUserId(context.getMessageSystem(), getAddress(), context.getDbAddress(), login);
        context.getMessageSystem().sendMessage(message);
    }

    public void addUser(int id, String name) {
        users.put(id, name);
        System.out.println("User: " + name + " has id: " + id);
    }
}
