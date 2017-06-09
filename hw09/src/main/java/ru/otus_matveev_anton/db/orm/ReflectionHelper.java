package ru.otus_matveev_anton.db.orm;

import ru.otus_matveev_anton.db.DataSet;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

class ReflectionHelper {
    static <T extends DataSet, V> Setter<T,V> getSetter(Field field){
        String fieldName = field.getName();
        try {
            return field.getDeclaringClass().getMethod(String.format("set%S%s", fieldName.substring(0,1), fieldName.substring(1)), field.getType())::invoke;
        } catch (NoSuchMethodException e) {
            return (obj, value)-> setFieldValue(field, obj, value);
        }
    }

    static <T extends DataSet, V> Getter<T,V> getGetter(Field field){
        String fieldName = field.getName();

        try {
            Method method = field.getDeclaringClass().getMethod(String.format("get%S%s", fieldName.substring(0, 1), fieldName.substring(1)));
            return (obj) ->(V) method.invoke(obj);
        } catch (NoSuchMethodException e) {
            return  (obj)-> getFieldValue(field, obj);
        }
    }

    private static <T extends DataSet, R> void setFieldValue(Field field, T obj, R value){
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

    private static <T extends DataSet, R> R getFieldValue(Field field, T obj){
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

    static <T extends DataSet> String getTableName(Class<T> clazz){
        Table table = clazz.getDeclaredAnnotation(Table.class);
        String tableName;
        if (table == null || (tableName = table.name()).isEmpty()) {
            tableName = clazz.getSimpleName();
        }
        return tableName;
    }

    static String getColumnName(Field field){
        String fieldName;
        Column column = field.getAnnotation(Column.class);
        if (column == null || (fieldName = column.name()).isEmpty()) {
            fieldName = field.getName();
        }
        return fieldName;
    }

    static String getColumnDefinition(Field field){
        StringBuilder fieldDefB = new StringBuilder(getColumnName(field))
                .append(' ');

        Column column = field.getAnnotation(Column.class);
        Class columnType = field.getType();

        if (column == null) {
            return fieldDefB.append(toSqlType(columnType)).toString();
        }

        if (!column.columnDefinition().isEmpty()){
            return fieldDefB.append(column.columnDefinition()).toString();
        }

        fieldDefB.append(toSqlType(columnType));
        if (String.class.equals(columnType)){
            fieldDefB.append('(').append(column.length()).append(')');
        }

        if (!column.nullable()){
            fieldDefB.append(" NOT NULL");
        }

        return fieldDefB.toString();
    }

    private static String toSqlType(Class clazz){
        if (Integer.class.equals(clazz) || int.class.equals(clazz)){
            return "INTEGER";
        }else if (Long.class.equals(clazz) || long.class.equals(clazz)){
            return "BIGINT";
        }else if (String.class.equals(clazz)){
            return "VARCHAR";
        }else if (Short.class.equals(clazz) || short.class.equals(clazz)){
            return "SMALLINT";
        }
        throw new IllegalArgumentException("field type must be one of Long, String, Short, Integer");
    }
}
