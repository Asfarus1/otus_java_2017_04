package ru.otus_matveev_anton.db.my_orm;

public interface Mapper<T extends DataSet> {
    void save(T dataSet);

    T get(long id);
}
