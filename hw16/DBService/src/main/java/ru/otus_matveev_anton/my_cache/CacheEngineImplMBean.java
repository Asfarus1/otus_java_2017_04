package ru.otus_matveev_anton.my_cache;

public interface CacheEngineImplMBean {

    long getTimeThresholdS();

    void setTimeThresholdS(long timeThresholdS);

    int getMaxElements();

    void setMaxElements(int maxElements);

    long getLifeTimeS();

    void setLifeTimeS(long lifeTimeS);

    long getIdleTimeS();

    void setIdleTimeS(long idleTimeS);

    boolean isEternal();

    void setEternal(boolean eternal);

    long getHitCount();

    long getMissCount();

    int getSize();
}
