package ru.otus_matveev_anton.db;


import ru.otus_matveev_anton.db.data_sets.UserDataSet;

public interface DBService{
    void saveUser(UserDataSet user);

    UserDataSet getUser(long id);

    void shutdown();
}
