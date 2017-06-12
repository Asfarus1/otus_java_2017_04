package ru.otus_matveev_anton.db.my_orm;

import java.sql.ResultSet;

public interface ResultHandler<T> {
    T handle(ResultSet resultSet) throws Exception;
}
