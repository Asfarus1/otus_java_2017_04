package ru.otus_matveev_anton.db.orm;

import ru.otus_matveev_anton.db.DataSet;

public interface Mapper<T extends DataSet> {
    void save(T dataSet) throws Exception;

    T get(long id);
}
