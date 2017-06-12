package ru.otus_matveev_anton.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import ru.otus_matveev_anton.db.data_sets.UserDataSet;
import ru.otus_matveev_anton.db.data_sets.UserWithAdressAndPhonesDateSet;

public class DBServiceHibernateXMLImpl implements DBService {
    private final SessionFactory sessionFactory;

    public DBServiceHibernateXMLImpl() {
        Configuration cfg = new Configuration();
        cfg.configure("/hibernate.cfg.xml");
        this.sessionFactory = createSessionFactory(cfg);
    }

    private SessionFactory createSessionFactory(Configuration cfg) {
        StandardServiceRegistryBuilder builder = cfg.getStandardServiceRegistryBuilder();
        builder.applySettings(cfg.getProperties());
        StandardServiceRegistry serviceRegistry = builder.build();
        return cfg.buildSessionFactory(serviceRegistry);
    }

    @Override
    public void saveUser(UserDataSet user) {
        try (Session session = sessionFactory.openSession()){
            session.save(user);
        }
    }

    @Override
    public UserWithAdressAndPhonesDateSet getUser(long id) {
        try(Session session = sessionFactory.openSession()){
            return session.load(UserWithAdressAndPhonesDateSet.class, id);
        }
    }

    @Override
    public void shutdown() {
        sessionFactory.close();
    }
}
