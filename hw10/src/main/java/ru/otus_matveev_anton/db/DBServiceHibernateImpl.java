package ru.otus_matveev_anton.db;

import org.hibernate.cfg.Configuration;
import ru.otus_matveev_anton.db.data_sets.AdressDataSet;
import ru.otus_matveev_anton.db.data_sets.PhoneDataSet;
import ru.otus_matveev_anton.db.data_sets.UserWithAdressAndPhonesDateSet;

public class DBServiceHibernateImpl extends DBServiceHibernate {

    @Override
    public Configuration configure() {
        Configuration cfg = new Configuration();
        cfg.addAnnotatedClass(AdressDataSet.class);
        cfg.addAnnotatedClass(PhoneDataSet.class);
        cfg.addAnnotatedClass(UserWithAdressAndPhonesDateSet.class);

        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/example");
        cfg.setProperty("hibernate.connection.username", "postgres");
        cfg.setProperty("hibernate.connection.password", "postgres");
        cfg.setProperty("hibernate.show_sql", "true");
        cfg.setProperty("hibernate.format_sql", "true");
        cfg.setProperty("hibernate.hbm2ddl.auto", "create");
        cfg.setProperty("hibernate.connection.useSSL", "false");
        cfg.setProperty("hibernate.enable_lazy_load_no_trans", "true");

        return cfg;
    }
}