package ru.otus_matveev_anton.messages;


public class CacheStatsDataSet extends CachePropsDataSet{
    private long HitCount;
    private long MissCount;
    private int Size;

    public long getHitCount() {
        return HitCount;
    }

    public void setHitCount(long hitCount) {
        HitCount = hitCount;
    }

    public long getMissCount() {
        return MissCount;
    }

    public void setMissCount(long missCount) {
        MissCount = missCount;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }
}
