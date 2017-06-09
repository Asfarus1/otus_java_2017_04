package ru.otus_matveev_anton.db.orm;

import ru.otus_matveev_anton.db.DataSet;
import ru.otus_matveev_anton.db.Executor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MapperFactoryImpl implements MapperFactory{
    private final Map<Class<?>, SoftReference<Mapper>> mappers;

    public MapperFactoryImpl() {
        this.mappers = new ConcurrentHashMap<>();
    }

    @Override
    public <T extends DataSet> Mapper<T> get(Class<T> clazz) {
        SoftReference<Mapper> ref = mappers.get(clazz);
        Mapper<T> mapper = null;
        if (ref == null){
            mapper = createMapper(clazz);
            mappers.putIfAbsent(clazz, new SoftReference<>(mapper));
        }
        return mapper;
    }

    @Override
    public <T extends DataSet> void createTable(Class<T> clazz) {

    }

    private <T extends DataSet> Mapper<T> createMapper(Class<T> clazz) {
        String tableName = getTableName(clazz);

        Field[] fields = clazz.getFields();

        final StringBuilder updateB = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        final StringBuilder insertB = new StringBuilder("INSERT INTO ").append(tableName).append("(");

        HandlerBuilder<T> handlerBuilder = (rs) -> {
            T obj = clazz.newInstance();
            obj.setId(rs.getLong("id"));
            return obj;
        };

        ArgsSetterBuilder<T> argsSetterBuilder = (lst, obj) -> {};
        int columnCount = 0;

        for (Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers()) && !field.isAnnotationPresent(Id.class)) {
                final String columnName = getColumnName(field);

                handlerBuilder = handlerBuilder.andThen((obj,rs)-> getSetter(field).set(obj, rs.getObject(columnName)));

                argsSetterBuilder = argsSetterBuilder.andThen((lst, obj)->lst.add(getGetter(field).get(obj)));

                updateB.append(columnName).append("=?,");
                insertB.append(columnName).append(',');
                columnCount++;
            }
        }

        updateB.setCharAt(updateB.length()-1,' ');
        updateB.append("WHERE id=?");

        insertB.setCharAt(insertB.length()-1, ')');
        insertB.append(" VALUES(");
        for (int i = 0; i < columnCount; i++) {
            insertB.append("?,");
        }
        insertB.setCharAt(insertB.length()-1, ')');

        final String query = String.format("SELECT * FROM %s WHERE id=?", tableName);
        final String update = updateB.toString();
        final String insert = insertB.toString();
        final ArgsSetterBuilder<T> argsSetter = argsSetterBuilder;
        final HandlerBuilder<T> handler = handlerBuilder;

        return new Mapper<T>() {

            @Override
            public void save(T dataSet, Connection connection) throws Exception {
                List args = new ArrayList();
                argsSetter.aply(args, dataSet);
                Executor executor = new Executor(connection);
                if (dataSet.getId() == 0){
                    long newId = executor.ExecuteWithReturningKey(insert, args.toArray(new Object[args.size()]));
                    dataSet.setId(newId);
                }else {
                    Object[] params =  args.toArray(new Object[args.size() + 1]);
                    params[params.length-1] = dataSet.getId();
                    executor.ExecuteUpdate(update, params);
                }
            }

            @Override
            public T get(long id, Connection connection) {
                Executor executor = new Executor(connection);
                return executor.ExecuteQuery(query, handler::handle, id);
            }
        };
    }

    private interface HandlerBuilder<T extends DataSet>{
        T handle(ResultSet resultSet) throws Exception;

        default HandlerBuilder<T> andThen(Setter<T, ResultSet> after) {
            Objects.requireNonNull(after);
            return (rs) -> {
                T res = handle(rs);
                after.set(res, rs);
                return res;
            };
        }
    }

    private interface ArgsSetterBuilder<T extends DataSet>{
        void aply(List lst, T obj) throws Exception;

        default ArgsSetterBuilder andThen(ArgsSetterBuilder<T> after) {
            Objects.requireNonNull(after);
            return (lst, obj) -> {
                aply(lst, (T) obj);
                after.aply(lst, (T) obj);
            };
        }
    }

    private <T extends DataSet, V> Setter<T,V> getSetter(Field field){
        String fieldName = field.getName();
        try {
            return field.getDeclaringClass().getDeclaredMethod(String.format("set%S%s" + fieldName.substring(0,1), fieldName.substring(1)), field.getType())::invoke;
        } catch (NoSuchMethodException e) {
            return (obj, value)-> setFieldValue(field, obj, value);
        }
    }

    private <T extends DataSet, V> Getter<T,V> getGetter(Field field){
        String fieldName = field.getName();

        try {
            Method method = field.getDeclaringClass().getDeclaredMethod(String.format("get%S%s" + fieldName.substring(0, 1), fieldName.substring(1)), field.getType());
            return (obj) -> {
                V res = (V) method.invoke(obj);
                return res;
            };
        } catch (NoSuchMethodException e) {
            return  (obj)-> getFieldValue(field, obj);
        }
    }

    private <T extends DataSet, R> void setFieldValue(Field field, T obj, R value){
        boolean accessible = field.isAccessible();
        if (!accessible){
            field.setAccessible(true);
        }
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }finally {
            if (!accessible){
                field.setAccessible(false);
            }
        }
    }

    private <T extends DataSet, R> R getFieldValue(Field field, T obj){
        boolean accessible = field.isAccessible();
        if (!accessible){
            field.setAccessible(true);
        }
        try {
            return (R) field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }finally {
            if (!accessible){
                field.setAccessible(false);
            }
        }
        return null;
    }

    private <T extends DataSet, R> String getTableName(Class<T> clazz){
        Table table = clazz.getDeclaredAnnotation(Table.class);
        String tableName;
        if (table == null || (tableName = table.name()).isEmpty()) {
            tableName = clazz.getSimpleName();
        }
        return tableName;
    }

    private String getColumnName(Field field){
        String fieldName;
        Column column = field.getAnnotation(Column.class);
        if (column == null || (fieldName = column.name()) == null || fieldName.isEmpty()) {
            fieldName = field.getName();
        }
        return fieldName;
    }
}
