package ru.otus_matveev_anton.db;

import ru.otus_matveev_anton.db.data_sets.UserDataSet;
import ru.otus_matveev_anton.db.my_orm.MapperFactory;
import ru.otus_matveev_anton.db.my_orm.MyOrmConfig;

public class DBServiceMyOrmImpl implements DBService{
    private final MapperFactory factory;

    public DBServiceMyOrmImpl() {
        this.factory = new MyOrmConfig("/connection.cfg", "/MyOrmConf.cfg").getFactory();
        factory.createTableQuery(UserDataSet.class);
    }

    @Override
    public void saveUser(UserDataSet user) {
        factory.get(UserDataSet.class).save(user);
    }

    @Override
    public UserDataSet getUser(long id) {
        return factory.get(UserDataSet.class).get(id);
    }

    @Override
    public void shutdown() {
        factory.getConfiguration().close();
    }
}
