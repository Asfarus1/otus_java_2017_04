package ru.otus_matveev_anton.db.my_cache;


public interface CacheEngine<K, V>{

    void put(MyElement<K, V> element);

    MyElement<K, V> get(K key);

    void dispose();
}
