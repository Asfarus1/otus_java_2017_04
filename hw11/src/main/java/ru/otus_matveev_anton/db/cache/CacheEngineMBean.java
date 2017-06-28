package ru.otus_matveev_anton.db.cache;

public interface CacheEngineMBean {
    int getTimeThresholdMs();

    int getMaxElements();

    void setMaxElements(int maxElements);

    long getLifeTimeMs();

    void setLifeTimeMs(long lifeTimeMs);

    long getIdleTimeMs();

    void setIdleTimeMs(long idleTimeMs);

    boolean isEternal();

    void setEternal(boolean eternal);

    int getHitCount();

    int getMissCount();

    int size();
}
