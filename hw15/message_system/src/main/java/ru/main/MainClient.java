package ru.main;

import ru.otus_matveev_anton.genaral.AddresseeImpl;
import ru.otus_matveev_anton.genaral.MessageSystemClient;
import ru.otus_matveev_anton.genaral.SpecialAddress;
import ru.otus_matveev_anton.message_system_client.JsonSocketClient;

import java.io.IOException;

/**
 * Created by asfarus on 10.08.2017.
 */
public class MainClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        MessageSystemClient client = new JsonSocketClient("/");
        client.init();
        client.addMessageReceiveListener(m->{System.out.println(m);return true;});
        AddresseeImpl all = new AddresseeImpl(SpecialAddress.ALL.name(), "testGroup");
        while (true) {
//            client.sendMessage(all, "ssss");
            Thread.sleep(1000);
        }
    }
}
