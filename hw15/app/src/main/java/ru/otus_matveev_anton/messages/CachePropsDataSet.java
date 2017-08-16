package ru.otus_matveev_anton.messages;

public class CachePropsDataSet {
    private long TimeThresholdS;
    private int MaxElements;
    private long LifeTimeS;
    private long IdleTimeS;
    private boolean Eternal;

    public long getTimeThresholdS() {
        return TimeThresholdS;
    }

    public void setTimeThresholdS(long timeThresholdS) {
        TimeThresholdS = timeThresholdS;
    }

    public int getMaxElements() {
        return MaxElements;
    }

    public void setMaxElements(int maxElements) {
        MaxElements = maxElements;
    }

    public long getLifeTimeS() {
        return LifeTimeS;
    }

    public void setLifeTimeS(long lifeTimeS) {
        LifeTimeS = lifeTimeS;
    }

    public long getIdleTimeS() {
        return IdleTimeS;
    }

    public void setIdleTimeS(long idleTimeS) {
        IdleTimeS = idleTimeS;
    }

    public boolean isEternal() {
        return Eternal;
    }

    public void setEternal(boolean eternal) {
        Eternal = eternal;
    }
}
