package ru.otus_matveev_anton;

public class CachePropsDataSet {
    private int TimeThresholdS;
    private int MaxElements;
    private int LifeTimeS;
    private int IdleTimeS;
    private boolean Eternal;

    public int getTimeThresholdS() {
        return TimeThresholdS;
    }

    public void setTimeThresholdS(int timeThresholdS) {
        TimeThresholdS = timeThresholdS;
    }

    public int getMaxElements() {
        return MaxElements;
    }

    public void setMaxElements(int maxElements) {
        MaxElements = maxElements;
    }

    public int getLifeTimeS() {
        return LifeTimeS;
    }

    public void setLifeTimeS(int lifeTimeS) {
        LifeTimeS = lifeTimeS;
    }

    public int getIdleTimeS() {
        return IdleTimeS;
    }

    public void setIdleTimeS(int idleTimeS) {
        IdleTimeS = idleTimeS;
    }

    public boolean isEternal() {
        return Eternal;
    }

    public void setEternal(boolean eternal) {
        Eternal = eternal;
    }
}
