package ru.otus_matveev_anton.db.dao;

import ru.otus_matveev_anton.db.data_sets.UserDataSet;

public interface UserDao<T extends UserDataSet> {
    T get(long id);

    void save(T user);
}
