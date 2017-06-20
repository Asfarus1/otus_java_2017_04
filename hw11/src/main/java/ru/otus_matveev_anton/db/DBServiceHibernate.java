package ru.otus_matveev_anton.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import ru.otus_matveev_anton.db.data_sets.UserDataSet;
import ru.otus_matveev_anton.db.data_sets.UserWithAdressAndPhonesDateSet;

abstract public class DBServiceHibernate implements DBService{
    private final SessionFactory sessionFactory;

    DBServiceHibernate() {
        Configuration cfg = configure();
        this.sessionFactory = createSessionFactory(cfg);
    }

    abstract Configuration configure();

    public void saveUser(UserDataSet user) {
        try (Session session = sessionFactory.openSession()){
            session.save(user);
        }
    }

    public UserWithAdressAndPhonesDateSet getUser(long id) {
        try(Session session = sessionFactory.openSession()){
            return session.load(UserWithAdressAndPhonesDateSet.class, id);
        }
    }

    public void shutdown() {
        sessionFactory.close();
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }
}
