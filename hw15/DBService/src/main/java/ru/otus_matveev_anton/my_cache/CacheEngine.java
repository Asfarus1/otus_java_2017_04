package ru.otus_matveev_anton.my_cache;


public interface CacheEngine<K, V>{

    void put(MyElement<K, V> element);

    MyElement<K, V> get(K key);

    void dispose();

    void addCacheStatsChangedListener(CacheStatsChangedListener listener);

    boolean removeCacheStatsChangedListener(CacheStatsChangedListener listener);
}
