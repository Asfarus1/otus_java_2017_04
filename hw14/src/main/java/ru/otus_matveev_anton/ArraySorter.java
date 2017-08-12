package ru.otus_matveev_anton;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

class ArraySorter  {

    private final static int countThreads = 4;

    private static class Segment{
        private int[] arr;
        int start;
        int end;

        Segment(int[] arr, int start, int end) {
            this.arr = arr;
            this.start = start;
            this.end = end;
        }

        Segment insertSort() {
            int up = start;
            int val,pos;
            for (;up < end;up++) {
                for (int i = up + 1; i <= end; i++) {
                    if (arr[up] > arr[i]) {
                        val = arr[i];
                        pos = binarySearchNotGrowThen(val, up);
                        System.arraycopy(arr, pos, arr, pos + 1, i - pos);
                        arr[pos] = val;
                        up++;
                    }
                }
            }
            return this;
        }

        private int binarySearchNotGrowThen(int n, int to){
            int mid;
            int from = this.start;
            while (to > from){
                mid = (to + from)/2;
                if (n < arr[mid]){
                    to = mid;
                }else {
                    from = mid + 1;
                }
            }
            return from;
        }

        Segment merge(int endSecond) {
            int startFirst = start;
            int startSecond = end + 1;
            end = endSecond;

            int[] tempArr = new int[startSecond - startFirst];
            System.arraycopy(arr, start, tempArr, 0, tempArr.length);

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

            return this;
        }
    }

    static void sort(int[] arr) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(countThreads);
        int step = arr.length/countThreads;
        int i;
        Queue<Future<Segment>> sorters = new LinkedList<>();
        for (i = 0; i < countThreads - 1; i++) {
            sorters.offer(executor.submit(new Segment(arr, i * step, (i + 1) * step - 1)::insertSort));
        }
        sorters.offer(executor.submit(new Segment(arr, i * step, arr.length - 1)::insertSort));
        while (true){
            Segment segment1 = sorters.poll().get();
            if (sorters.isEmpty()){
                break;
            }
            Segment segment2 = sorters.poll().get();
            if (segment1.start < segment2.start){
                sorters.offer(executor.submit(()->segment1.merge(segment2.end)));
            }else {
                sorters.offer(executor.submit(()->segment2.merge(segment1.end)));
            }
        }
        executor.shutdown();
    }
}
