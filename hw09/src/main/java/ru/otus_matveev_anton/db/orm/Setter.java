package ru.otus_matveev_anton.db.orm;

import ru.otus_matveev_anton.db.DataSet;

public interface Setter<T extends DataSet, V> {
    void set(T obj, V value) throws Exception;
}
