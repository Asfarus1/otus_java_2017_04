package ru.otus_matveev_anton;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

class ArraySorter  {

    private final static int countThreads = 4;

    private static class Segment{
        int start;
        int end;

        Segment(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class InsertSort implements Callable<ArraySorter.Segment>{
        private int[] arr;
        private int start;
        private int end;

        private InsertSort(int[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        @Override
        public Segment call() throws Exception {
            int up = start;
            int val,pos;
            for (;up < end;up++) {
                for (int i = up + 1; i <= end; i++) {
                    if (arr[up] > arr[i]) {
                        val = arr[i];
                        pos = binarySearchNotGrowThen(arr, val, start, up);
                        System.arraycopy(arr, pos, arr, pos + 1, i - pos);
                        arr[pos] = val;
                        up++;
                    }
                }
            }
            return new Segment(start, end);
        }

        private int binarySearchNotGrowThen(int[] arr, int n, int start, int end){
            int mid;
            while (end > start){
                mid = (end + start)/2;
                if (n < arr[mid]){
                    end = mid;
                }else {
                    start = mid + 1;
                }
            }
            return start;
        }
    }

    static void sort(int[] arr) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(countThreads);
        int step = arr.length/countThreads;
        int i;
        Queue<Future<Segment>> sorters = new LinkedList<>();
        for (i = 0; i < countThreads - 1; i++) {
            sorters.offer(executor.submit(new InsertSort(arr, i * step, (i + 1) * step - 1)));
        }
        sorters.offer(executor.submit(new InsertSort(arr, i * step, arr.length - 1)));
        while (true){
            Segment segment1 = sorters.poll().get();
            if (sorters.isEmpty()){
                break;
            }
            Segment segment2 = sorters.poll().get();
            if (segment1.start < segment2.start){
                sorters.offer(executor.submit(new Merge(arr, segment1.start, segment1.end, segment2.start, segment2.end)));
            }else {
                sorters.offer(executor.submit(new Merge(arr, segment2.start, segment2.end, segment1.start, segment1.end)));
            }
        }
        executor.shutdown();
    }

    private static class Merge implements Callable<Segment>{
        private int[] arr;
        private int startFirst;
        private int endFirst;
        private int startSecond;
        private int endSecond;

        /**
         *
         * @param arr - arr to merge
         * @param startFirst  - start of array's segment to sort
         * @param endFirst < startSecond
         * @param startSecond  =  endFirst + 1
         * @param endSecond - < startSecond
         */
        private Merge(int[] arr, int startFirst, int endFirst, int startSecond, int endSecond) {
            this.arr = arr;
            this.startFirst = startFirst;
            this.endFirst = endFirst;
            this.startSecond = startSecond;
            this.endSecond = endSecond;
        }

        @Override
        public Segment call() throws Exception {
            Segment segment = new Segment(startFirst, endSecond);
            int[] tempArr = new int[endFirst - startFirst + 1];
            System.arraycopy(arr, startFirst, tempArr, 0, tempArr.length);
            int tempArrIndex = 0;
            int endTemp = tempArr.length - 1;
            while (tempArrIndex <= endTemp && startSecond <= endSecond){
                if (tempArr[tempArrIndex] < arr[startSecond]){
                    arr[startFirst++] = tempArr[tempArrIndex++];
                }else {
                    arr[startFirst++] = arr[startSecond++];
                }
            }

            while (tempArrIndex <= endTemp){
                arr[startFirst++] = tempArr[tempArrIndex++];
            }

            while (startSecond <= endSecond){
                arr[startFirst++] = arr[startSecond++];
            }

            return segment;
        }
    }
}
