package ru.main;

import ru.otus_matveev_anton.genaral.AddresseeImpl;
import ru.otus_matveev_anton.genaral.MessageSystemClient;
import ru.otus_matveev_anton.message_system_client.JsonSocketClient;

import java.io.IOException;

public class MainClient1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        MessageSystemClient client = JsonSocketClient.fromConfigFiles("/message_system_client1.properties");
        client.init();
        client.addMessageReceiveListener(m->{System.out.println(m);return true;});
        AddresseeImpl all = new AddresseeImpl("1", "testGroup");
        while (true) {
            client.sendMessage(all, "ssss");
            Thread.sleep(1000);
        }
    }
}