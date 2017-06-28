package ru.otus_matveev_anton.db.cache;


public interface CacheEngine<K, V> extends CacheEngineMBean{

    void put(MyElement<K, V> element);

    MyElement<K, V> get(K key);

    void dispose();
}
