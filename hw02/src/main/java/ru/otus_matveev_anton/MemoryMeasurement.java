package ru.otus_matveev_anton;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by asfarus on 10.04.2017.
 */
public class MemoryMeasurement {

    private static final int DEFAULT_ITER_COUNT = 12_000_000;
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

    private void waitFinalize(){
        System.gc();
        try {
            for (int i = 0; i < 10; i++) {
                if (!isObjectInMemory){
                    System.out.println("Wrapper очищен сборщиком");
                    break;
                }
                Thread.sleep(10);
            }
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T> MeasurementResult makeMeasurement(Supplier<T> initFunc){
        return makeMeasurement(initFunc, null);
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
