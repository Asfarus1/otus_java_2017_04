package ru.otus_matveev_anton;

import ru.otus_matveev_anton.db.DBService;
import ru.otus_matveev_anton.db.DBServiceMyOrmImpl;
import ru.otus_matveev_anton.db.data_sets.UserDataSet;

import java.io.IOException;
import java.sql.SQLException;


public class MainMyOrm {
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {

        UserDataSet user = new UserDataSet();
        user.setAge(29);
        user.setName("Вася");
        System.out.println(user);

        DBService dbService = new DBServiceMyOrmImpl();

        dbService.saveUser(user);
        System.out.println("after save: " + user);
        long id = user.getId();

        user = dbService.getUser(id);
        System.out.println("after get: " + user);

        user.setName("Афанасий");
        user.setAge(12);
        dbService.saveUser(user);
        user = dbService.getUser(id);
        System.out.println("after сhange name and age: " + user);
        dbService.shutdown();
    }
}
