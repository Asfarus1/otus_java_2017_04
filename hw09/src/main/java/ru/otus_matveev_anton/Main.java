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
        Class.forName(prop.getProperty("driver"));

        try(Connection connection = DriverManager.getConnection(prop.getProperty("url"),prop)){

        }
    }
}
