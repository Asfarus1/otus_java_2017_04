package ru.otus_matveev_anton.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import ru.otus_matveev_anton.db.dao.UserDaoHibernateImpl;
import ru.otus_matveev_anton.db.data_sets.AdressDataSet;
import ru.otus_matveev_anton.db.data_sets.PhoneDataSet;
import ru.otus_matveev_anton.db.data_sets.UserDataSet;
import ru.otus_matveev_anton.db.data_sets.UserWithAdressAndPhonesDateSet;

public class DBServiceHibernateImpl implements DBService{
    private final SessionFactory sessionFactory;

    public DBServiceHibernateImpl() {
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

        sessionFactory = createSessionFactory(cfg);
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    @Override
    public void saveUser(UserDataSet user) {
        try(Session session = sessionFactory.openSession()){
            new UserDaoHibernateImpl(session).save((UserWithAdressAndPhonesDateSet)user);
        }
    }

    @Override
    public UserWithAdressAndPhonesDateSet getUser(long id) {
        try(Session session = sessionFactory.openSession()){
            return new UserDaoHibernateImpl(session).get(id);
        }
    }

    @Override
    public void shutdown() {
        sessionFactory.close();
    }
}
