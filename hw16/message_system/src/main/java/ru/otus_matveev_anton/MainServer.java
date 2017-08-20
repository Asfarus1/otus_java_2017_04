package ru.otus_matveev_anton;

import ru.otus_matveev_anton.genaral.MessageSystem;
import ru.otus_matveev_anton.json_message_system.JsonSocketServer;

public class MainServer {

    private static final int PORT = 5050;

    public static void main(String[] args) throws Exception {
        MessageSystem msServer = new JsonSocketServer(PORT);
        msServer.start();
    }
}
