package ru.otus_matveev_anton.db.my_orm;

public interface Setter<T extends DataSet, V> {
    void set(T obj, V value) throws Exception;
}
