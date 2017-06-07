package ru.otus_matveev_anton.db;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SimpleDAO {
    private Connection connection;
    private ConcurrentHashMap<Class<?>, SoftReference<Object>> loaders;
    private ConcurrentHashMap<Class<?>, SoftReference<Object>> savers;


    public <T extends DataSet> T load(long id, Class<T> clazz){
       Table table = clazz.getDeclaredAnnotation(Table.class);
       String tableName;
       if (table == null || (tableName = table.name()).isEmpty()){
           tableName = clazz.getSimpleName();
       }
       ResultHandler<T> handler = (rs)->{
           T result = null;
           try {
               result = clazz.newInstance();
           } catch (InstantiationException|IllegalAccessException e) {
               throw new DBException(e);
           }
           String columnName;
           if (rs.next()){
               Field[] fields = clazz.getFields();
               for (Field field : fields) {
                   if (Modifier.isTransient(field.getModifiers())){
                       Column column = field.getDeclaredAnnotation(Column.class);
                       if (column == null || (columnName = table.name()).isEmpty()){
                           columnName = field.getName();
                       }
                       rs.getObject(columnName);
                   }
               }
           }
           return result;
       };
       Executor executor = new Executor(connection);
       return executor.ExecuteQuery("", handler);
    }

    public <T extends DataSet> void save(long id, Class<T> clazz){
        Table table = clazz.getDeclaredAnnotation(Table.class);
        String tableName;
        if (table == null || (tableName = table.name()).isEmpty()){
            tableName = clazz.getSimpleName();
        }
        ResultHandler<T> handler = (rs)->{
            T result = null;
            try {
                result = clazz.newInstance();
            } catch (InstantiationException|IllegalAccessException e) {
                throw new DBException(e);
            }
            String columnName;
            if (rs.next()){
                Field[] fields = clazz.getFields();
                for (Field field : fields) {
                    if (Modifier.isTransient(field.getModifiers())){
                        Column column = field.getDeclaredAnnotation(Column.class);
                        if (column == null || (columnName = table.name()).isEmpty()){
                            columnName = field.getName();
                        }
                        rs.getObject(columnName);
                    }
                }
            }
            return result;
        };
        Executor executor = new Executor(connection);
        executor.ExecuteQuery("", handler);
    }
}
