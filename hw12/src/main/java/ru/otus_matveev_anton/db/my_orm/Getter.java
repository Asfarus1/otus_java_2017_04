package ru.otus_matveev_anton.db.my_orm;

public interface Getter<T,V> {
    V get(T obj) throws Exception;
}
