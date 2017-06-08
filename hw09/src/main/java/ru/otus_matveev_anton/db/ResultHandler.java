package ru.otus_matveev_anton.db;

import java.sql.ResultSet;

public interface ResultHandler<T> {
    T handle(ResultSet resultSet) throws Exception;
}
