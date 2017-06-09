package ru.otus_matveev_anton.db;

public interface Dao {
    <T extends DataSet> T load(long id, Class<T> clazz);

    <T extends DataSet> void save(T dataSet, Class<T> clazz);

    <T extends DataSet> void createTableIfNotExists(Class<T> clazz);
}
