package ru.otus_matveev_anton.db;


import ru.otus_matveev_anton.db.my_orm.DataSet;

public interface DBService{
    <T extends DataSet> void save(T dataSet, Class<T> clazz);

    <T extends DataSet> T get(long id, Class<T> clazz);

    void shutdown();
}
