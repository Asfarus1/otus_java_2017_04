package ru.otus_matveev_anton.my_cache;

public class MyElement<K, V> {
    private final K key;
    private final V value;
    private final long creationTime;
    private volatile long lastAccessTime;

    public MyElement(K key, V value) {
        this.key = key;
        this.value = value;
        this.creationTime = getCurrentTime();
        this.lastAccessTime = getCurrentTime();
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setAccessed() {
        lastAccessTime = getCurrentTime();
    }

    @Override
    public String toString() {
        long ct = getCurrentTime();
        return "MyElement{" +
                "key=" + key +
                ", value=" + value +
                ", creation=" + (ct - creationTime)/1000 + " c." +
                ", lastAccess=" + (ct - lastAccessTime)/1000  + "c." +
                '}';
    }
}
