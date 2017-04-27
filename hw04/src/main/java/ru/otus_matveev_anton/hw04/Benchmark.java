package ru.otus_matveev_anton.hw04;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matveev.AV1 on 26.04.2017.
 */
public class Benchmark implements BenchmarkMBean {
    private int countAddedElemPerIter = 3;
    private int countRemovedElemPerIter = 1;

    @Override
    public int getCountRemovedElemPerIter() {
        return countRemovedElemPerIter;
    }

    @Override
    public void setCountRemovedElemPerIter(int count) {
        this.countRemovedElemPerIter = count;
    }

    public int getCountAddedElemPerIter() {
        return countAddedElemPerIter;
    }

    public void setCountAddedElemPerIter(int count) {
        this.countAddedElemPerIter = count;
    }

    void run(){
        List<String> memoryDevourer = new LinkedList<>();
        int i;
        while (true) {
            for (i = 0; i < getCountAddedElemPerIter(); i++) {
                memoryDevourer.add(new String(new char[0]));
            }
            for (i = 0; i < getCountRemovedElemPerIter();i++){
                memoryDevourer.remove(0);
            }
        }
    }
}
