package ru.otus_matveev_anton.atm;

/**
 * Created by Matveev.AV1 on 24.05.2017.
 */
public interface Cell {

    int getBalance();

    boolean widthdraw(int requred);

    void download();
}
