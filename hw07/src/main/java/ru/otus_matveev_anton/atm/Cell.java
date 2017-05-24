package ru.otus_matveev_anton.atm;

public interface Cell{

    int getNominal();

    int getCount();

    void withdraw(int count);

    void download();
}
