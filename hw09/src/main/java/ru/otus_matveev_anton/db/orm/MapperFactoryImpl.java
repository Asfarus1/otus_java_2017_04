package ru.otus_matveev_anton.db.orm;

import ru.otus_matveev_anton.db.Configuration;
import ru.otus_matveev_anton.db.DataSet;
import ru.otus_matveev_anton.db.Executor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ru.otus_matveev_anton.db.orm.ReflectionHelper.*;

public class MapperFactoryImpl implements MapperFactory{
    private final Map<Class<?>, SoftReference<Mapper>> mappers;
    private final Configuration configuration;

    public MapperFactoryImpl(Configuration configuration) {
        this.mappers = new ConcurrentHashMap<>();
        this.configuration = configuration;
    }

    @Override
    public <T extends DataSet> Mapper<T> get(Class<T> clazz) {
        SoftReference<Mapper> ref = mappers.get(clazz);
        Mapper<T> mapper;
        if (ref == null) {
            mapper = createMapper(clazz);
            mappers.putIfAbsent(clazz, new SoftReference<>(mapper));
        }else {
            mapper = ref.get();
        }
        return mapper;
    }

    @Override
    public <T extends DataSet> String createTableQuery(Class<T> clazz){
        String tableName = getTableName(clazz);
        String createPattern = configuration.getCreatePattern();

        StringBuilder createB = new StringBuilder();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers()) && !field.isAnnotationPresent(Id.class)) {
                createB.append(',').append(getColumnDefinition(field));
            }
        }

        return String.format(createPattern, tableName, createB.toString());
    }

    private <T extends DataSet> Mapper<T> createMapper(Class<T> clazz) {
        String tableName = getTableName(clazz);

        final StringBuilder updateB = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        final StringBuilder insertB = new StringBuilder("INSERT INTO ").append(tableName).append("(");

        HandlerBuilder<T> handlerBuilder = (rs) -> {
            T obj = clazz.newInstance();
            rs.next();
            obj.setId(rs.getLong("id"));
            return obj;
        };

        ArgsSetterBuilder<T> argsSetterBuilder = (lst, obj) -> {};
        int columnCount = 0;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!Modifier.isTransient(field.getModifiers()) && !field.isAnnotationPresent(Id.class)) {
                String columnName = getColumnName(field);
                Column column = field.getAnnotation(Column.class);

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
            public void save(T dataSet) throws Exception {
                List args = new ArrayList();
                argsSetter.aply(args, dataSet);
                Executor executor = new Executor(configuration);
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
            public T get(long id) {
                Executor executor = new Executor(configuration);
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
}
