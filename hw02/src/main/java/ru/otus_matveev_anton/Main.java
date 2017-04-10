package ru.otus_matveev_anton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asfarus on 10.04.2017.
 */
public class Main {
    public static void main(String[] args) {
        MemoryMeasurement.MeasurementResult stringSize = MemoryMeasurement.makeMeasurement(()->new String(""));
        System.out.printf("Размер пустой строки = %d байт на %d итерациях%n", stringSize.initSize, stringSize.iterCount, stringSize.addedSizePerIter);

        MemoryMeasurement.MeasurementResult arraySize = MemoryMeasurement.makeMeasurement(()->new int[0]/*, (ar, i)->ar[i] = i*/);
        System.out.printf("Размер int[0] массива = %d байт на %d итерациях, рост памяти за элемент = %d байт%n", arraySize.initSize, arraySize.iterCount, arraySize.addedSizePerIter);

        MemoryMeasurement.MeasurementResult arrayListSize = MemoryMeasurement.makeMeasurement(ArrayList<Integer>::new, (ar, i)->ar.add(i));
        System.out.printf("Размер ArrayList<Integer> = %d байт на %d итерациях, рост памяти за элемент = %d байт%n", arrayListSize.initSize, arrayListSize.iterCount, arrayListSize.addedSizePerIter);

        MemoryMeasurement.MeasurementResult mapSize = MemoryMeasurement.makeMeasurement(HashMap<Integer, Integer>::new, (ar, i)->ar.put(i,i));
        System.out.printf("Размер HashMap<Integer, Integer> массива = %d байт на %d итерациях, рост памяти за элемент = %d байт%n", mapSize.initSize, mapSize.iterCount, mapSize.addedSizePerIter);

    }
}
