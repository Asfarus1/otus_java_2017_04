package ru.otus_matveev_anton;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created by asfarus on 10.04.2017.
 */
public class MemoryMeasurement {

    private static final int DEFAULT_ITER_COUNT = 5_000_000;

    public static class MeasurementResult {
        public final long initSize;
        public final long addedSizePerIter;
        public final int iterCount;

        public MeasurementResult(long initSize, long addedSizePerIter, int iterCount) {
            this.initSize = Math.round(initSize/(double)iterCount);
            this.addedSizePerIter = Math.round(addedSizePerIter/(double)iterCount);
            this.iterCount = iterCount;
        }
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc) {
        return makeMeasurement(initFunc, null);
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc, BiConsumer<T, Integer> iterFunc) {
        long usedMemory;
        long usedInitMemory = 0;
        long usedIterMemory = 0;

        final Runtime runtime = Runtime.getRuntime();

        Object[] arr;
        int i;

        {
            arr = new Object[DEFAULT_ITER_COUNT];
            usedMemory = runtime.totalMemory() - runtime.freeMemory();
            for (i = 0; i < arr.length; i++) {
                arr[i] = initFunc.get();
            }
        }
        usedInitMemory += (runtime.totalMemory() -  runtime.freeMemory()) - usedMemory;
        System.gc();

        if (iterFunc != null) {
            usedMemory = runtime.freeMemory();
            T obj = initFunc.get();
            for (i = 0; i < DEFAULT_ITER_COUNT; i++) {
                iterFunc.accept(obj, i);
            }
            usedIterMemory += usedMemory - runtime.freeMemory();
        }

        System.gc();
        return new MeasurementResult(usedInitMemory, usedIterMemory, DEFAULT_ITER_COUNT);
    }

}
