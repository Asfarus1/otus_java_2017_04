package ru.otus_matveev_anton;


import ru.otus_matveev_anton.app.DBService;
import ru.otus_matveev_anton.app.FrontendService;
import ru.otus_matveev_anton.app.MessageSystemContext;
import ru.otus_matveev_anton.messageSystem.Address;
import ru.otus_matveev_anton.messageSystem.MessageSystem;

public class MSMain {
    public static void main(String[] args) throws InterruptedException {
        MessageSystem messageSystem = new MessageSystem();

        MessageSystemContext context = new MessageSystemContext(messageSystem);
        Address frontAddress = new Address("Frontend");
        context.addDbAddress(frontAddress);
        Address dbAddress = new Address("DB");
        context.addDbAddress(dbAddress);

        FrontendService frontendService = new FrontendServiceImpl(context, frontAddress);
        frontendService.init();

        DBService dbService = new DBServiceImpl(context, dbAddress);
        dbService.init();

        messageSystem.start();

        frontendService.handleRequest("tully");
        frontendService.handleRequest("sully");
    }
}
