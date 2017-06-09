package ru.otus_matveev_anton;

import ru.otus_matveev_anton.db.Configuration;
import ru.otus_matveev_anton.db.Dao;
import ru.otus_matveev_anton.user.UserDataSet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

//для имитации орм рефлекшном создаю анонимные классы с методами для сохранения извлечения класса и сохраняю их в мапу
//работает с ограниченным количеством типов, не поддерживает связи обьектов
public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        Properties prop = new Properties();
        prop.load(Main.class.getClassLoader().getResourceAsStream("connection.cfg"));
        Class.forName(prop.getProperty("driver"));

        UserDataSet user = new UserDataSet();
        user.setAge(29);
        user.setName("Вася");
        System.out.println(user);

        try(Connection connection = DriverManager.getConnection(prop.getProperty("url"),prop)){
            Configuration configuration = new Configuration(connection);
            configuration.setShowSql(true);
            configuration.setLog(System.out);

            Dao dao = configuration.getSimpleDao();
            dao.createTableIfNotExists(UserDataSet.class);

            dao.save(user, UserDataSet.class);
            System.out.println("after save: " + user);
            long id = user.getId();

            user = dao.load(id, UserDataSet.class);
            System.out.println("after load: " + user);

            user.setName("Афанасий");
            user.setAge(12);
            dao.save(user, UserDataSet.class);
            user = dao.load(id, UserDataSet.class);
            System.out.println("after сhange name and age: " + user);
        }
    }
}
