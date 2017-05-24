package ru.otus_matveev_anton.atm;

public class CellImpl implements Cell {
    private final int capacity;
    private final int nominal;
    private int count;

    public CellImpl(int capacity, int nominal) {
        this.capacity = capacity;
        this.nominal = nominal;
        this.count = capacity;
    }

    public int getNominal() {
        return nominal;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void download() {
        count = this.capacity;
    }

    @Override
    public void withdraw(int count){
        this.count -= count;
    }
}
