package ru.otus_matveev_anton.db.dao;

import ru.otus_matveev_anton.db.data_sets.UserDataSet;
import ru.otus_matveev_anton.db.my_orm.Mapper;
import ru.otus_matveev_anton.db.my_orm.MapperFactory;

public class UserDaoMyOrmImpl implements UserDao {
    private final Mapper<UserDataSet> mapper;

    public UserDaoMyOrmImpl(MapperFactory factory) {
        mapper = factory.get(UserDataSet.class);
    }

    @Override
    public UserDataSet get(long id) {
        return mapper.get(id);
    }

    @Override
    public void save(UserDataSet user) {
        mapper.save(user);
    }
}
