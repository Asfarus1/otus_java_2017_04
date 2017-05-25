package ru.otus_matveev_anton.atm;

public class WithdrawAlgorithmMaxBigNominals implements WithdrawAlgorithm {

    @Override
    public boolean withdrawFromSequenceCells(int requested, Cell... cells) {
        return cells.length != 0 && withdrawFromSequenceCells(cells, 0 , requested);
    }

    //ищем первую комбинацию в которой максимально число банкнот большего номинала
    //цикл нужен для случая больший номинал не кратен меньшему, допустим номиналы 5 и 2 при требуемой сумме 6
    private boolean withdrawFromSequenceCells(Cell[] cells, int initPos, int requested) {
        Cell cell = cells[initPos];
        int nominal = cell.getNominal();
        int currentNominalCount = Math.min(requested / nominal, cell.getCount());

        withdrawNextCell:
        if (currentNominalCount * nominal != requested) {

            if (initPos < cells.length - 1) {
                for(;currentNominalCount > -1; currentNominalCount--) {
                    if (withdrawFromSequenceCells(cells, initPos + 1, requested - currentNominalCount * nominal)) {
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
