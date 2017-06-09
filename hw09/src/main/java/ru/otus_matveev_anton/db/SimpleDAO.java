package ru.otus_matveev_anton.db;

import ru.otus_matveev_anton.db.orm.MapperFactory;
import ru.otus_matveev_anton.db.orm.MapperFactoryImpl;

import java.sql.Connection;

public class SimpleDAO {
    private Connection connection;
    private final MapperFactory factory;;

    public SimpleDAO(Connection connection) {
        this.connection = connection;
        factory = new MapperFactoryImpl();
    }

    public <T extends DataSet> T load(long id, Class<T> clazz){
       return factory.get(clazz).get(id, connection);
    }

    public <T extends DataSet> void save(T dataSet, Class<T> clazz){
        try {
            factory.get(clazz).save(dataSet, connection);
        } catch (Exception e) {
           throw new DBException(e);
        }
    }

    public <T extends DataSet> void createTableIfNotExists(Class<T> clazz){
//        factory.
    }
}
