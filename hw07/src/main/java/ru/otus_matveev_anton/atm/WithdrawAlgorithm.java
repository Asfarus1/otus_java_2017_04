package ru.otus_matveev_anton.atm;

public interface WithdrawAlgorithm {
    boolean withdrawFromSequenceCells(int requested, Cell... cells);
}
