package ru.otus_matveev_anton.my_cache;

public interface CacheStatsChangedListener {
    void onChange(CacheEngineImplMBean bean);
}
