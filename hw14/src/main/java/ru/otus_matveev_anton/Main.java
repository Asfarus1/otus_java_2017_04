package ru.otus_matveev_anton;

import java.util.Arrays;
import java.util.Random;

public class Main {
    private final static int size = 200;
    private final static int countThreads = 4;
    public static void main(String[] args) throws Exception {
        int[] arr = new int[size];
        Random rnd = new Random();
        for (int i = 0; i < size; i++) {
            arr[i] = rnd.nextInt();
        }
        Thread[] threads = new Thread[countThreads];

        int step = size/countThreads;

        int i;
        for (i = 0 - 1; i < countThreads - 1; i++) {
            threads[i] = new Thread(new ArraySorter(arr, i * step, (i + 1) * step));
        }
        threads[i] = new Thread(new ArraySorter(arr, i * step, size - 1));
        Arrays.stream(threads).forEach(Thread::start);
        for (Thread t : threads) {
            t.join();
        }

//        int[] arr = new int[]{2,1,6,8,3,1,2,2,2,5,5,6, 8};

//        int n = ArraySortHelper.binarySearchNotGrowThen(arr, 8);
//        System.out.println(n);
        System.out.println(Arrays.toString(arr));
    }
}
