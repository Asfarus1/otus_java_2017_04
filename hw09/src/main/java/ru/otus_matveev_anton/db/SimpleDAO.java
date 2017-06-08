package ru.otus_matveev_anton.db;

import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleDAO {
    private Connection connection;
    private ConcurrentHashMap<Class<?>, SoftReference<Object>> loaders;
    private ConcurrentHashMap<Class<?>, SoftReference<Object>> savers;


    public <T extends DataSet> T load(long id, Class<T> clazz){
       Executor executor = new Executor(connection);
       return executor.ExecuteQuery("", null);
    }

    public <T extends DataSet> void save(long id, Class<T> clazz){
        Executor executor = new Executor(connection);
        executor.ExecuteQuery("", null);
    }
}
