package ru.otus_matveev_anton;

public class ArraySorter implements Runnable {

    private int[] arr;
    private int start;
    private int end;

    ArraySorter(int[] arr, int start, int end) {
        this.arr = arr;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        int up = start;
        int val,pos;
        for (;up < end;up++) {
            for (int i = up + 1; i < end; i++) {
                if (arr[up] > arr[i]) {
                    val = arr[i];
                    pos = binarySearchNotGrowThen(arr, val, start, i - 1);
                    System.arraycopy(arr, pos, arr, pos + 1, i - pos);
                    arr[up++] = val;
                }
            }
        }
    }

    private int binarySearchNotGrowThen(int[] arr, int n, int start, int end)
    {
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

    /**
     *
     * @param arr - arr to merge
     * @param startFirst  < endFirst
     * @param endFirst < startSecond
     * @param startSecond < endSecond
     * @param endSecond - ///
     */
    private static void merge(int[] arr, int startFirst, int endFirst, int startSecond, int endSecond){

        int[] tempArr = new int[endFirst - startFirst];
        System.arraycopy(arr, startFirst, tempArr, 0, tempArr.length);
        int tempArrIndex = 0;
        int endTemp = tempArr.length - 1;
        while (tempArrIndex < endTemp && startSecond < endSecond){
            if (tempArr[tempArrIndex] < arr[startSecond]){
                arr[startFirst++] = tempArr[tempArrIndex++];
            }else {
                arr[startFirst++] = arr[startSecond++];
            }
        }

        while (tempArrIndex < endTemp){
            arr[startFirst++] = tempArr[tempArrIndex++];
        }

        while (startSecond < endSecond){
            arr[startFirst++] = arr[startSecond];
        }
    }
}
