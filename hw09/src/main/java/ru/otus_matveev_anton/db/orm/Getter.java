package ru.otus_matveev_anton.db.orm;

public interface Getter<T,V> {
    V get(T obj) throws Exception;
}
