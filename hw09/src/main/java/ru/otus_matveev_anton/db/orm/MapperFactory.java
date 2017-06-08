package ru.otus_matveev_anton.db.orm;

import ru.otus_matveev_anton.db.DataSet;

public interface MapperFactory {
    <T extends DataSet> Mapper<T> get(Class<T> clazz);
}