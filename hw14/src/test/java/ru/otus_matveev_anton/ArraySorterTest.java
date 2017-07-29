package ru.otus_matveev_anton;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class ArraySorterTest {
    @Test
    public void sort() throws Exception {
        int[] arr = {3, 2, 43, 1, 2, 4, 67, 5, 76, 89, 34, 34, 66, 65, 54, 3, 6, 76};
        int[] arr2 = arr.clone();
        ArraySorter.sort(arr);

        System.out.println(Arrays.toString(arr));
        Arrays.parallelSort(arr2);
        Assert.assertArrayEquals(arr, arr2);
    }
}