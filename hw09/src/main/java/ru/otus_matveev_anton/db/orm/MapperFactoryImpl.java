package ru.otus_matveev_anton.db.orm;

import ru.otus_matveev_anton.db.DataSet;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MapperFactoryImpl implements MapperFactory{
    private final Map<Class<?>, SoftReference<Mapper>> mappers;

    public MapperFactoryImpl() {
        this.mappers = new ConcurrentHashMap();
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

    private static class ColumnMapper<T extends DataSet, V>{

        private String fieldName;

        Getter<T, V> getter;

        Setter<T, V> setter;
    }

    private <T extends DataSet> Mapper<T> createMapper(Class<T> clazz) {
        Table table = clazz.getDeclaredAnnotation(Table.class);
        String tableName;
        if (table == null || (tableName = table.name()).isEmpty()) {
            tableName = clazz.getSimpleName();
        }

        List<ColumnMapper> columnMapperList = new ArrayList<>();
        Field[] fields = clazz.getFields();

        final StringBuilder updateB = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        final StringBuilder insertB = new StringBuilder("INSERT INTO ").append(tableName).append("(id,");

        for (Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers()) && !field.isAnnotationPresent(Id.class)) {

                ColumnMapper<T, Object> columnMapper = new ColumnMapper<>();

                Column column = field.getAnnotation(Column.class);
                if (column == null || (columnMapper.fieldName = table.name()) == null || columnMapper.fieldName.isEmpty()) {
                    columnMapper.fieldName = field.getName();
                }
                columnMapper.setter = getSetter(field);
                columnMapper.getter = getGetter(field);

                columnMapperList.add(columnMapper);

                updateB.append(columnMapper.fieldName).append("=?,");
                insertB.append(columnMapper.fieldName).append(',');
            }
        }

        updateB.setCharAt(updateB.length()-1,' ');
        updateB.append("WHERE id=?");

        insertB.setCharAt(insertB.length()-1, ')');
        insertB.append(" VALUES(");
        columnMapperList.forEach((s)->insertB.append("?,"));
        insertB.setCharAt(insertB.length()-1, ')');

        HandlerBuilder<T> handlerBuilder = (rs) -> {
            T obj = clazz.newInstance();
            obj.setId(rs.getLong("id"));
            return obj;
        };

        for (ColumnMapper columnMapper : columnMapperList) {
            handlerBuilder = handlerBuilder.andThen((obj,rs)-> columnMapper.setter.set(obj, rs.getObject(columnMapper.fieldName)));
        }

        final String query = String.format("SELECT * FROM %s WHERE id=", tableName);
        final String update = updateB.toString();
        final String insert = insertB.toString();

        return new Mapper<T>() {
            @Override
            public void save(T dataSet) {

            }

            @Override
            public T get(long id) {
                return null;
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
}
