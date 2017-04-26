package ru.otus_matveev_anton;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matveev.AV1 on 26.04.2017.
 */
public class BenchMark implements BenchmarkMBean {
    private int size;
    private int memoryGrowthRate;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getMemoryGrowthRate() {
        return memoryGrowthRate;
    }

    /**
     * чем меньше тем быстрее заполнеется память
     * @param memoryGrowthRate
     */
    public void setMemoryGrowthRate(int memoryGrowthRate) {
        this.memoryGrowthRate = memoryGrowthRate;
    }

    void run(){
        List memoryDevourer = new LinkedList();
        int currentSize = size;
        Object[] array = new Object[currentSize];

        System.out.printf("Array of size: %d created; linked list size %d%n", currentSize, memoryDevourer.size());
        int i = 0;
        while (i < Integer.MAX_VALUE) {
            array[i % currentSize] = new String(new char[0]);
            i++;
            if (i % memoryGrowthRate == 0){
                memoryDevourer.add(new String(new char[0]));
            }
            if (i % currentSize == 0){
                currentSize = size;
                array = new Object[currentSize];
                System.out.printf("Array of size: %d created; linked list size %d%n", currentSize, memoryDevourer.size());
            }
        }
    }
}
