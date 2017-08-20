package ru.otus_matveev_anton.db.my_orm;

public interface MapperFactory {
    <T extends DataSet> Mapper<T> get(Class<T> clazz);

    <T extends DataSet> void createTableQuery(Class<T> clazz);

    MyOrmConfig getConfiguration();
}
