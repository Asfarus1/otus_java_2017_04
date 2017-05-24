package ru.otus_matveev_anton.atm;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class ATMImpl implements ATM {
    private Set<Cell> cells = new TreeSet<>((o1, o2) -> Integer.compare(o2.getNominal(), o1.getNominal()));

    public ATMImpl(Collection<Cell> cells) {
        this.cells.addAll(cells);
    }

    @Override
    public int getBalance() {
        return cells.stream().mapToInt((cell) -> cell.getNominal() * cell.getCount()).reduce(Integer::sum).orElse(0);
    }

    @Override
    public void download() {
        this.cells.forEach(Cell::download);
    }

    //ищем первую комбинацию в которой максимально число банкнот большего номинала
    //цикл нужен для случая больший номинал не кратен меньшему, допустим номиналы 5 и 2 при требуемой сумме 6
    @Override
    public boolean withdraw(int requred) {
        if (!cells.isEmpty()) {
            Cell[] cellArray = cells.toArray(new Cell[cells.size()]);

            class cellSequence {

                private boolean withdraw(int initPos, int requred) {
                    Cell cell = cellArray[initPos];
                    int nominal = cell.getNominal();
                    int currentNominalCount = Math.min(requred / nominal, cell.getCount());

                    withdrawNextCell:
                    if (currentNominalCount * nominal != requred) {

                        if (initPos < cellArray.length - 1) {
                            for(;currentNominalCount > -1; currentNominalCount--) {
                                if (withdraw(initPos + 1, requred - currentNominalCount * nominal)) {
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

            return new cellSequence().withdraw(0, requred);
        }
        return false;
    }
}
