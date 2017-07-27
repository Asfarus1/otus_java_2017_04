package ru.otus_matveev_anton;

/**
 * Created by Matveev.AV1 on 27.07.2017.
 */
public class ArraySortHelper {
    public static void sort(int[] arr){

    }

    private static int GetMinrun(int n)
    {
        int r = 0;           /* станет 1 если среди сдвинутых битов будет хотя бы 1 ненулевой */
        while (n >= 64) {
            r |= n & 1;
            n >>= 1;
        }
        return n + r;
    }
}
