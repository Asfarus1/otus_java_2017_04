package ru.otus_matveev_anton.atm;

public interface ATM {
    int getBalance();

    boolean withdraw(int requested);

    void download();

    void setAlgorithm(WithdrawAlgorithm algorithm);
}
