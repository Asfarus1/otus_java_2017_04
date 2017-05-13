package ru.asfarus.hw03;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MyList<T> implements List<T>, Serializable, RandomAccess{

    private static final int INITIAL_CAPACITY = 10;
    private Object[] arr;
    private int size;

    @SuppressWarnings("unchecked")
    public MyList(int intialCapacity) {
        this.arr = new Object[intialCapacity];
    }

    public MyList() {
        this(INITIAL_CAPACITY);
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    private String outOfBoundsMsg(int var1) {
        return String.format("Index: %d, Size^ %d", var1, this.size());
    }

    public boolean contains(Object o) {
        return this.indexOf(o) != -1;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        return (T[])Arrays.copyOf(arr, this.size);
    }

    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(T1[] t1s) {
        if (t1s.length < size) {
            return (T1[]) Arrays.copyOf(this.arr, size, t1s.getClass());
        }
        System.arraycopy(this.arr, 0, t1s, 0, this.size());
        if (this.size() < t1s.length) {
            Arrays.fill(t1s, this.size(), t1s.length, null);
        }
        return t1s;
    }

    private void addSize(int needSize){
        if (needSize <= this.arr.length){
            return;
        }
        int newSize = arr.length + arr.length >> 1;
        newSize = newSize<needSize?needSize:newSize;
        arr = Arrays.copyOf(this.arr, newSize);
    }

    private void checkIndex(int ind){
        if (ind < 0 || ind >= size()){
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean add(T t) {
        addSize(this.size + 1);
        this.arr[this.size++] = t;
        return true;
    }

    public void clear() {
        Arrays.fill(this.arr, null);
        this.size = 0;
    }

    @SuppressWarnings("unchecked")
    public T get(int i) {
        checkIndex(i);
        return (T)this.arr[i];
    }

    @SuppressWarnings("unchecked")
    public T set(int i, T t) {
        checkIndex(i);
        T res = (T)this.arr[i];
        this.arr[i] = t;
        return res;
    }

    public T remove(int i) {
        checkIndex(i);
        return fastRemove(i);
    }

    @SuppressWarnings("unchecked")
    private T fastRemove(int i){
        T res = (T)this.arr[i];
        if (i < this.size - 1){
            System.arraycopy(this.arr, i + 1, this.arr, i, this.size - i - 1);
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
        if (i < 0 || i > this.size){
            throw new IndexOutOfBoundsException();
        }
        addSize(this.size + 1);
        if (i < this.size){
            System.arraycopy(this.arr, i, this.arr, i + 1, this.size++ - i);
            this.arr[i] = t;
        }else {
            this.arr[this.size++] = t;
        }
    }

    public int indexOf(Object o) {
        for (int i = 0; i < this.size; i++) {
            if (Objects.equals(this.arr[i], o)){
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        for (int i = this.size - 1; i > -1; i--) {
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
        addSize(this.size + count);
        if (i < this.size()){
            System.arraycopy(this.arr, i, this.arr, i + count, this.size - i);
        }
        for (T elem : collection) {
            this.arr[i++] = elem;
        }
        this.size+= count;
        return !collection.isEmpty();
    }

    public boolean removeAll(Collection<?> collection) {
        Objects.requireNonNull(collection);
        return removeIf(collection::contains);
    }

    public boolean retainAll(Collection<?> collection) {
        Objects.requireNonNull(collection);
        return removeIf((e) ->!collection.contains(e));
    }

    @SuppressWarnings("unchecked")
    public boolean removeIf(Predicate<? super T> filter){
        Objects.requireNonNull(filter);
        int newIndex = 0, index = 0;
        for (;index < this.size; index ++){
            if (!filter.test((T) this.arr[index])){
                this.arr[newIndex++] = this.arr[index];
            }
        }
        if (newIndex < this.size){
            this.arr = Arrays.copyOf(this.arr, newIndex);
            this.size = newIndex;
            return true;
        }
        return false;
    }

    private class ListIter implements Iterator<T>, ListIterator<T>{
        int pos = 0;
        int currentItem = -1;

        public ListIter() {
        }

        ListIter(int pos) {
            this.pos = pos;
        }

        public boolean hasPrevious() {
            return pos > 0;
        }

        @SuppressWarnings("unchecked")
        public T previous() {
            return (T) MyList.this.arr[currentItem = --pos];
        }

        public int nextIndex() {
            return pos;
        }

        public int previousIndex() {
            return pos - 1;
        }

        public void set(T t) {
            MyList.this.arr[pos] = t;
        }

        public void add(T t) {
            MyList.this.add(pos++, t);
            currentItem = -1;
        }

        @Override
        public boolean hasNext() {
            return pos < MyList.this.size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            return (T)MyList.this.arr[currentItem = pos++];
        }

        @Override
        public void remove() {
            MyList.this.remove(pos = currentItem);
            currentItem = -1;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            final int len = MyList.this.size();
            for (int i = 0; i < len; i++) {
                action.accept((T) MyList.this.arr[i]);
            }
        }
    }

    public Iterator<T> iterator() {
        return new ListIter();
    }

    public ListIterator<T> listIterator() {
        return new ListIter(0);
    }

    public ListIterator<T> listIterator(int i) {
        return new ListIter(i);
    }

    public List<T> subList(int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super T> c) {
        Arrays.sort((T[]) this.arr, 0, this.size(), c);
    }
}
