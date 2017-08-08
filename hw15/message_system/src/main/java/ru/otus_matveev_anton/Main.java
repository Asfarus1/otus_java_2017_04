package ru.otus_matveev_anton;

import ru.otus_matveev_anton.addressing.AddresseeImpl;
import ru.otus_matveev_anton.message_system.JsonMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asfarus on 08.08.2017.
 */
public class Main {
    public static void main(String[] args) {
        String groupName1 = "group1";
        Addressee from = new AddresseeImpl("ANYONE", groupName1);
        Addressee to = new AddresseeImpl("3", groupName1);
        List<Double> pair = new ArrayList<>();
        pair.add(2d);
        pair.add(4d);
        JsonMessage message = new JsonMessage(from, to, pair);

        String packagedData = message.toPackagedData();
        System.out.println(packagedData);

        JsonMessage message1 = new JsonMessage();
        message1.loadFromPackagedData(packagedData);
        String packagedData1 = message1.toPackagedData();
        System.out.println(packagedData1);
    }
}
