package ru.otus_matveev_anton.hw04;

/**
 * Created by Matveev.AV1 on 26.04.2017.
 */
public interface BenchmarkMBean {
    int getCountAddedElemPerIter();

    void setCountAddedElemPerIter(int count);

    int getCountRemovedElemPerIter();

    void setCountRemovedElemPerIter(int count);
}
