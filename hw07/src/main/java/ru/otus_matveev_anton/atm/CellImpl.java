package ru.otus_matveev_anton.atm;

/**
 * Created by Matveev.AV1 on 24.05.2017.
 */
public class CellImpl implements Cell {
    private final int capacity;
    private final int nominal;
    private int count;
    private Cell next;

    public CellImpl(int capacity, int nominal) {
        this.capacity = capacity;
        this.nominal = nominal;
        this.count = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getNominal() {
        return nominal;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getBalance() {
        return 0;
    }

    public Cell getNext() {
        return next;
    }

    public void setNext(Cell next) {
        this.next = next;
    }

    @Override
    public void download() {
        count = this.capacity;
    }

    public boolean widthdraw(int requred){
        int currentNominalCount = Math.min(requred / getNominal(), getCount());
        int sum = count * getBalance();
        if (sum == requred){
//            ret
        }
        Cell nextCell = getNext();
        for (; currentNominalCount > -1 ; currentNominalCount--) {
            sum = count * getBalance();
            if (requred != sum){

                if (nextCell == null || nextCell.widthdraw(requred - sum)){
                    continue;
                }
            }
            count-=currentNominalCount;
            return true;
        }
        return false;
    }
}
