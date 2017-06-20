package ru.otus_matveev_anton.db.dao;

import org.hibernate.Session;
import ru.otus_matveev_anton.db.data_sets.UserWithAdressAndPhonesDateSet;

public class UserDaoHibernateImpl implements UserDao<UserWithAdressAndPhonesDateSet>{
    private final Session session;

    public UserDaoHibernateImpl(Session session) {
        this.session = session;
    }

    @Override
    public UserWithAdressAndPhonesDateSet get(long id) {
        return session.load(UserWithAdressAndPhonesDateSet.class, id);
    }

    @Override
    public void save(UserWithAdressAndPhonesDateSet user) {
        session.save(user);
    }
}
