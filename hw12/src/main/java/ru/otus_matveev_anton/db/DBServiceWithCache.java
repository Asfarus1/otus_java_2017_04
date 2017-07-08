package ru.otus_matveev_anton.db;

import ru.otus_matveev_anton.db.data_sets.UserDataSet;
import ru.otus_matveev_anton.my_cache.CacheEngine;
import ru.otus_matveev_anton.my_cache.CacheEngineImpl;
import ru.otus_matveev_anton.my_cache.MyElement;

public class DBServiceWithCache implements DBService {
    private DBService service;
    private CacheEngine<Long, UserDataSet> cache;

    public DBServiceWithCache(DBService service) {
        this.service = service;
        this.cache = new CacheEngineImpl<>("users", "/MyCacheConf.cfg");
    }

    @Override
    public void saveUser(UserDataSet user) {
        service.saveUser(user);
        cache.put(new MyElement<>(user.getId(), user));
    }

    @Override
    public UserDataSet getUser(long id) {
        MyElement<Long, UserDataSet> cacheVal = cache.get(id);
        if (cacheVal == null){
            UserDataSet user = service.getUser(id);
            cache.put(new MyElement<>(id, user));
            return user;
        }
        return cacheVal.getValue();
    }

    @Override
    public void shutdown() {
        try {
            service.shutdown();
        }finally {
            cache.dispose();
        }
    }
}
