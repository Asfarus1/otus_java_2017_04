package ru.otus_matveev_anton.atm;

import java.util.Arrays;
import java.util.Collection;

//вынес алгоритм снятия денег из Cell в ATM т.к. считаю что ячейка просто просто хранит банкноты а атм решает как их выдавать
public class ATMImpl implements ATM {
    private final Cell[] cells;

    public ATMImpl(Collection<Cell> cells) {
        this.cells = cells.toArray(new Cell[cells.size()]);
        Arrays.sort(this.cells, (o1, o2) -> Integer.compare(o2.getNominal(), o1.getNominal()));
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
    public boolean withdraw(int requred) {
        return cells.length != 0 && withdrawFromSequenceCells(0,requred);
    }

    //ищем первую комбинацию в которой максимально число банкнот большего номинала
    //цикл нужен для случая больший номинал не кратен меньшему, допустим номиналы 5 и 2 при требуемой сумме 6
    private boolean withdrawFromSequenceCells(int initPos, int requred) {
        Cell cell = cells[initPos];
        int nominal = cell.getNominal();
        int currentNominalCount = Math.min(requred / nominal, cell.getCount());

        withdrawNextCell:
        if (currentNominalCount * nominal != requred) {

            if (initPos < cells.length - 1) {
                for(;currentNominalCount > -1; currentNominalCount--) {
                    if (withdrawFromSequenceCells(initPos + 1, requred - currentNominalCount * nominal)) {
                        break withdrawNextCell;
                    }
                }
            }
            return false;
        }
        if (currentNominalCount != 0) {
            cell.withdraw(currentNominalCount);
        }
        return true;
    }
}
