package ru.otus_matveev_anton;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by asfarus on 10.04.2017.
 */
public class MemoryMeasurement {

    private static final int DEFAULT_ITER_COUNT = 1_000_000;
    private boolean isObjectInMemory;

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

    private MemoryMeasurement() {
    }

    private void waitFinalize() {
        System.gc();
        for (int i = 0; i < 100; i++) {
            if (!isObjectInMemory) {
                break;
            }
            trySleep(1);
        }
        trySleep(1);
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc){
        return makeMeasurement(initFunc, null);
    }

    private static void trySleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc, Consumer<T> iterFunc){
        long usedMemory;
        long usedInitMemory = 0;
        long usedIterMemory = 0;

        final Runtime runtime = Runtime.getRuntime();
        MemoryMeasurement GCWaiter = new MemoryMeasurement();

        int i;
        {
            Object[] arr = new Object[DEFAULT_ITER_COUNT];
            Wrapper wrap = GCWaiter.new Wrapper(arr);
            usedMemory = runtime.totalMemory() - runtime.freeMemory();
            for (i = 0; i < arr.length; i++) {
                arr[i] = initFunc.get();
                if (i % 1024 == 0){
                    trySleep(1);
                }
            }
            usedInitMemory += (runtime.totalMemory() -  runtime.freeMemory()) - usedMemory;
        }
        GCWaiter.waitFinalize();

        if (iterFunc != null) {
            {
                usedMemory = runtime.totalMemory() - runtime.freeMemory();
                T obj = initFunc.get();
                Wrapper wrap = GCWaiter.new Wrapper(obj);
                for (i = 0; i < DEFAULT_ITER_COUNT; i++) {
                iterFunc.accept(obj);
                if (i % 1024 == 0){
                    trySleep(1);
                }
            }
                usedIterMemory += (runtime.totalMemory() -  runtime.freeMemory()) - usedMemory;
            }
            GCWaiter.waitFinalize();
        }

        return new MeasurementResult(usedInitMemory, usedIterMemory, DEFAULT_ITER_COUNT);
    }

    private class Wrapper{
        Object obj;

        public Wrapper(Object obj) {
            this.obj = obj;
            MemoryMeasurement.this.isObjectInMemory = true;
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            MemoryMeasurement.this.isObjectInMemory = false;
        }
    }
}
