package ru.otus_matveev_anton;

import java.util.ArrayList;

/**
 * Created by asfarus on 10.04.2017.
 */
//-Xms1024m -Xmx1024m
public class Main {
    public static void main(String[] args){
        MemoryMeasurement.MeasurementResult arraySize0 = MemoryMeasurement.makeMeasurement(()->new int[0]);
        System.out.printf("Размер int[0] массива = %d байт на %d итерациях%n", arraySize0.initSize, arraySize0.iterCount, arraySize0.addedSizePerIter);

        MemoryMeasurement.MeasurementResult arraySize10 = MemoryMeasurement.makeMeasurement(()->new int[10]);
        System.out.printf("Размер int[10] массива = %d байт на %d итерациях%n", arraySize10.initSize, arraySize10.iterCount, arraySize10.addedSizePerIter);

        MemoryMeasurement.MeasurementResult stringSize = MemoryMeasurement.makeMeasurement(()->new String(""));
        System.out.printf("Размер пустой строки = %d байт на %d итерациях%n", stringSize.initSize, stringSize.iterCount, stringSize.addedSizePerIter);

        MemoryMeasurement.MeasurementResult arrayListSize100 = MemoryMeasurement.makeMeasurement(() ->new ArrayList<>(100));
        System.out.printf("Размер LinkedList<Integer>(100) = %d байт на %d итерациях%n", arrayListSize100.initSize, arrayListSize100.iterCount, arrayListSize100.addedSizePerIter);

        MemoryMeasurement.MeasurementResult arrayListSize = MemoryMeasurement.makeMeasurement(() ->new ArrayList<>(1));
        System.out.printf("Размер ArrayList<Integer>(1) = %d байт на %d итерациях%n", arrayListSize.initSize, arrayListSize.iterCount, arrayListSize.addedSizePerIter);
    }
}
