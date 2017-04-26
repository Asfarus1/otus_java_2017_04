package ru.otus_matveev_anton.hw04;

/**
 * Created by Matveev.AV1 on 26.04.2017.
 */
public interface BenchmarkMBean {
    int getSize();

    void setSize(int size);

    int getMemoryGrowthRate();

    void setMemoryGrowthRate(int rate);
}
