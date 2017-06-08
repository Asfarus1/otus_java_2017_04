package ru.otus_matveev_anton;

import ru.otus_matveev_anton.db.SimpleDAO;
import ru.otus_matveev_anton.user.UserDataSet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        Properties prop = new Properties();
        prop.load(Main.class.getClassLoader().getResourceAsStream("db.cfg"));
        Class.forName(prop.getProperty("driver"));

        UserDataSet user = new UserDataSet();
        user.setAge(29);
        user.setName("Вася");
        try(Connection connection = DriverManager.getConnection(prop.getProperty("url"),prop)){
            SimpleDAO dao = new SimpleDAO(connection);
            dao.save(user, UserDataSet.class);
        }
    }
}
