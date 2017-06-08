package ru.otus_matveev_anton.db.orm;

import ru.otus_matveev_anton.db.DataSet;

import java.sql.Connection;

public interface Mapper<T extends DataSet> {
    void save(T dataSet, Connection connection) throws Exception;

    T get(long id, Connection connection);
}
