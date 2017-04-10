package ru.otus_matveev_anton;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created by asfarus on 10.04.2017.
 */
public class MemoryMeasurement {

    private static final int DEFAULT_ITER_COUNT = 1_000_000;

    public static class MeasurementResult {
        public final long initSize;
        public final long addedSizePerIter;
        public final int iterCount;

        public MeasurementResult(long initSize, long addedSizePerIter, int iterCount) {
            this.initSize = initSize;
            this.addedSizePerIter = addedSizePerIter;
            this.iterCount = iterCount;
        }
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc, BiConsumer<T, Integer> iterFunc) {
        return makeMeasurement(initFunc, iterFunc, DEFAULT_ITER_COUNT);
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc) {
        return makeMeasurement(initFunc, null, DEFAULT_ITER_COUNT);
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc, int iterCount) {
        return makeMeasurement(initFunc, null, iterCount);
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc, BiConsumer<T, Integer> iterFunc, int iterCount) {
        long freeMemory;
        long usedInitMemory = 0;
        long usedIterMemory = 0;

        final Runtime runtime = Runtime.getRuntime();

        T[] arr;
        int i;
        for (int j = 1 + iterCount / 10_000; j > -1; j--) {

            arr = (T[]) new Object[10_000];
            freeMemory = runtime.freeMemory();
            for (i = 0; i < arr.length; i++) {
                arr[i] = initFunc.get();
            }
            usedInitMemory += (freeMemory - runtime.freeMemory()) / arr.length;
        }

        if (iterFunc != null) {
            freeMemory = runtime.freeMemory();
            T obj = initFunc.get();
            for (i = 0; i < iterCount; i++) {
                iterFunc.accept(obj, i);
            }
            usedIterMemory += freeMemory - runtime.freeMemory();
        }

        return new MeasurementResult(usedInitMemory / iterCount, usedIterMemory / iterCount, iterCount);
    }

}
