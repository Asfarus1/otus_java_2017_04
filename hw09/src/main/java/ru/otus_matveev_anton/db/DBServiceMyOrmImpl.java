package ru.otus_matveev_anton.db;

import ru.otus_matveev_anton.db.data_sets.UserDataSet;
import ru.otus_matveev_anton.db.my_orm.DataSet;
import ru.otus_matveev_anton.db.my_orm.MapperFactory;
import ru.otus_matveev_anton.db.my_orm.MyOrmConfig;

public class DBServiceMyOrmImpl implements DBService {
    private final MapperFactory factory;

    public DBServiceMyOrmImpl() {
        this.factory = new MyOrmConfig("/connection.cfg", "/MyOrmConf.cfg").getFactory();
        factory.createTableQuery(UserDataSet.class);
    }

    @Override
    public <T extends DataSet> void save(T dataSet, Class<T> clazz) {
        factory.get(clazz).save(dataSet);
    }

    @Override
    public <T extends DataSet> T get(long id, Class<T> clazz) {
        return factory.get(clazz).get(id);
    }

    @Override
    public void shutdown() {
        factory.getConfiguration().close();
    }
}
