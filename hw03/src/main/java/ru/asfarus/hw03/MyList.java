package ru.asfarus.hw03;

import java.io.Serializable;
import java.util.*;

/**
 * Created by asfarus on 01.05.17.
 */
public class MyList<T> implements List<T>, Serializable, RandomAccess{

    private T[] arr;
    private int size;

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private String outOfBoundsMsg(int var1) {
        return "Index: " + var1 + ", Size: " + this.size();
    }

    public boolean contains(Object o) {
        return this.indexOf(o) != -1;
    }

    public T[] toArray() {
        return Arrays.copyOf(arr,size());
    }

    public <T1> T1[] toArray(T1[] t1s) {
        System.arraycopy(t1s, 0, this.arr, 0, size());
        Arrays.fill(t1s, size(), t1s.length, null);
        return t1s;
    }

    private void addSize(int needSize){
        if (needSize <= arr.length){
            return;
        }
        int newSize = arr.length + arr.length >> 1;
        newSize = newSize<needSize?needSize:newSize;
        arr = Arrays.copyOf(arr, newSize);
    }

    private void checkIndex(int ind){
        if (ind < 0 || ind >= size()){
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean add(T t) {
        addSize(size() + 1);
        this.arr[this.size++] = t;
        return true;
    }

    public void clear() {
        Arrays.fill(this.arr, null);
        this.size = 0;
    }

    public T get(int i) {
        checkIndex(i);
        return this.arr[i];
    }

    public T set(int i, T t) {
        checkIndex(i);
        T res = this.arr[i];
        this.arr[i] = t;
        return res;
    }

    public T remove(int i) {
        checkIndex(i);
        return fastRemove(i);
    }

    private T fastRemove(int i){
        T res = this.arr[i];
        if (i < size() - 1){
            System.arraycopy(this.arr, i, this.arr, i + 1, size() - i - 1);
        }
        this.arr[--this.size] = null;
        return res;
    }

    public boolean remove(Object o) {
       int i = this.indexOf(o);
       if  (i == -1) {
           return false;
       }
       fastRemove(i);
       return true;
    }

    public void add(int i, T t) {
        if (i < 0 || i > size()){
            throw new IndexOutOfBoundsException();
        }
        addSize(size() + 1);
        if (i != size()){
            System.arraycopy(this.arr, i + 1, this.arr, i, size() - i);
        }
    }

    public int indexOf(Object o) {
        for (int i = 0; i < size(); i++) {
            if (Objects.equals(this.arr[i], o)){
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        for (int i = size() - 1; i > -1; i--) {
            if (Objects.equals(this.arr[i], o)){
                return i;
            }
        }
        return -1;
    }

    public boolean containsAll(Collection<?> collection) {
        for (Object o : collection) {
            if (this.indexOf(o) == -1){
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends T> collection) {
       return addAll(this.size, collection);
    }

    public boolean addAll(int i, Collection<? extends T> collection) {
        int count = collection.size();
        addSize(this.size() + count);
        if (i < this.size()){
            System.arraycopy(this.arr, i + count, this.arr, i, this.size - i);
        }
        for (T elem : collection) {
            this.arr[i++] = elem;
        }
        return true;
    }

    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    public Iterator<T> iterator() {
        return null;
    }

    public ListIterator<T> listIterator() {
        return null;
    }

    public ListIterator<T> listIterator(int i) {
        return null;
    }

    public List<T> subList(int i, int i1) {
        return null;
    }
}
