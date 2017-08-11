package ru.main;

import ru.otus_matveev_anton.genaral.MessageSystem;
import ru.otus_matveev_anton.json_message_system.JsonSocketServer;

public class MainServer {
    public static void main(String[] args) throws Exception {
        MessageSystem ms = new JsonSocketServer(5050);
        ms.start();
    }
}
