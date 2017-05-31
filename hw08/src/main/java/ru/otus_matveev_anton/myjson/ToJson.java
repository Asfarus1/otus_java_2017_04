package ru.otus_matveev_anton.myjson;

/**
 * Created by Matveev.AV1 on 31.05.2017.
 */
public interface ToJson<T> {
    String toJson(T obj);
}
