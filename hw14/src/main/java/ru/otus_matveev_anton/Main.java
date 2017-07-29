package ru.otus_matveev_anton;

import java.util.Arrays;
import java.util.Random;

public class Main {
    private final static int size = 2000;

    public static void main(String[] args) throws Exception {
        int[] arr = new int[size];
        Random rnd = new Random();
        for (int i = 0; i < size; i++) {
            arr[i] = rnd.nextInt(size * 2);
        }
        int[] arr2 = arr.clone();
        ArraySorter.sort(arr);

        System.out.println(Arrays.toString(arr));
        Arrays.parallelSort(arr2);
        System.out.println(Arrays.toString(arr2));
        System.out.println(Arrays.equals(arr, arr2));
    }
}
