package ru.otus_matveev_anton;

import ru.otus_matveev_anton.genaral.MessageSystem;
import ru.otus_matveev_anton.json_message_system.JsonSocketServer;

public class MainServer {
    public static void main(String[] args) throws Exception {
        MessageSystem msServer = new JsonSocketServer(5050);
//        MessageSystem msServer = new JsonSocketServerWithPing(5050);
        msServer.start();
    }
}
