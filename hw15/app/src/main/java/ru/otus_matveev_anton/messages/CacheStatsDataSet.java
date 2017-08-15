package ru.otus_matveev_anton.messages;


public class CacheStatsDataSet {
    private int HitCount;
    private int MissCount;
    private int Size;

    public int getHitCount() {
        return HitCount;
    }

    public void setHitCount(int hitCount) {
        HitCount = hitCount;
    }

    public int getMissCount() {
        return MissCount;
    }

    public void setMissCount(int missCount) {
        MissCount = missCount;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }
}
