package ru.otus_matveev_anton;

import ru.otus_matveev_anton.db.DBService;
import ru.otus_matveev_anton.db.DBServiceHibernateImpl;
import ru.otus_matveev_anton.db.DBServiceHibernateXMLImpl;
import ru.otus_matveev_anton.db.data_sets.AdressDataSet;
import ru.otus_matveev_anton.db.data_sets.PhoneDataSet;
import ru.otus_matveev_anton.db.data_sets.UserWithAdressAndPhonesDateSet;

import java.util.*;

public class MainHibernateXML {
    public static void main(String[] args) {
        DBService dbService = new DBServiceHibernateXMLImpl();

        Set<PhoneDataSet> phones = new HashSet<>();
        Collections.addAll(phones,
                new PhoneDataSet("12412313"),
                new PhoneDataSet("99999")
        );

        dbService.saveUser(new UserWithAdressAndPhonesDateSet("tully", 12, new AdressDataSet("карловкая", 3), phones));

        UserWithAdressAndPhonesDateSet dataSet = (UserWithAdressAndPhonesDateSet) dbService.getUser(1);
        System.out.println(dataSet);

        dbService.shutdown();
    }
}
