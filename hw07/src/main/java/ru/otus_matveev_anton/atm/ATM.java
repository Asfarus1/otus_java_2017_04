package ru.otus_matveev_anton.atm;

public interface ATM {
    int getBalance();

    boolean withdraw(int requested);

    void download();

    default void setAlgorithm(WithdrawAlgorithm algorithm){throw new UnsupportedOperationException();}
}
