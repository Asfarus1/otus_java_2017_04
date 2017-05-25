package ru.otus_matveev_anton.atm;

import java.util.Arrays;
import java.util.Collection;

//вынес алгоритм снятия денег из Cell в ATM т.к. считаю что ячейка просто просто хранит банкноты а атм решает как их выдавать
public class ATMImpl implements ATM {
    private final Cell[] cells;
    private WithdrawAlgorithm algorithm;

    public ATMImpl(Collection<Cell> cells) {
        this.cells = cells.toArray(new Cell[cells.size()]);
        Arrays.sort(this.cells, (o1, o2) -> Integer.compare(o2.getNominal(), o1.getNominal()));
    }

    public void setAlgorithm(WithdrawAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public int getBalance() {
        return Arrays.stream(cells).mapToInt((cell) -> cell.getNominal() * cell.getCount()).reduce(Integer::sum).orElse(0);
    }

    @Override
    public void download() {
        Arrays.stream(cells).forEach(Cell::download);
    }

    @Override
    public boolean withdraw(int requested) {
        return algorithm.withdrawFromSequenceCells(requested, cells);
    }
}
