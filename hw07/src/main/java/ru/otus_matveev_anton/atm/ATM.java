package ru.otus_matveev_anton.atm;

import ru.otus_matveev_anton.Bank;

/**
 * Created by Matveev.AV1 on 24.05.2017.
 */
public interface ATM {
    int getBalance();

    boolean withdraw(int requested);

    void download();

    void setBank(Bank bank);
}
