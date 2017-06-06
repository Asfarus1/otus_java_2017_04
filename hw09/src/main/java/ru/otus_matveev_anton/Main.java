package ru.otus_matveev_anton;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        Properties prop = new Properties();
        prop.load(Main.class.getClassLoader().getResourceAsStream("db.cfg"));
        String driverName = prop.getProperty("driver");
        Class.forName(driverName);
        String url = String.format("%s:%s/%s",
                driverName,
                prop.getProperty("server_url"),
                prop.getProperty("db_name"));

        try(Connection connection = DriverManager.getConnection(url,
                prop.getProperty("user"),
                prop.getProperty("password"))){

        }
    }
}
