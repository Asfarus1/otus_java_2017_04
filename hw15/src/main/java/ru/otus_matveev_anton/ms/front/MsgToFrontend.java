package ru.otus_matveev_anton.ms.front;

import ru.otus_matveev_anton.app.FrontendService;
import ru.otus_matveev_anton.messageSystem.Address;
import ru.otus_matveev_anton.messageSystem.Addressee;
import ru.otus_matveev_anton.messageSystem.Message;

public abstract class MsgToFrontend extends Message {
    public MsgToFrontend(Address from, Address to) {
        super(from, to);
    }

    @Override
    public void exec(Addressee addressee) {
        if (addressee instanceof FrontendService) {
            exec((FrontendService) addressee);
        }
    }

    public abstract void exec(FrontendService frontendService);
}