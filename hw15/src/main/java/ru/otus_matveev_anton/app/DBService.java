package ru.otus_matveev_anton.app;

/**
 * Created by tully.
 */
public interface DBService {
    void init();

    String getAllCacheData();

    String getReadOnlyCacheData();

    void save(String jsonCacheProperties);
}
