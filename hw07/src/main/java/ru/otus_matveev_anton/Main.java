package ru.otus_matveev_anton;

import ru.otus_matveev_anton.atm.ATM;
import ru.otus_matveev_anton.atm.ATMImpl;
import ru.otus_matveev_anton.atm.Cell;
import ru.otus_matveev_anton.atm.CellImpl;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();

        List<Cell> cells = new ArrayList<>();
        cells.add(new CellImpl(20, 1000));
        cells.add(new CellImpl(30, 500));
        cells.add(new CellImpl(40, 100));
        cells.add(new CellImpl(50, 50));
        cells.add(new CellImpl(100, 15));

        ATM atm1 = new ATMImpl(cells);
        System.out.println("Initial balance atm1: " + atm1.getBalance());

        cells = new ArrayList<>();
        cells.add(new CellImpl(20, 1000));
        cells.add(new CellImpl(30, 500));
        cells.add(new CellImpl(40, 100));
        cells.add(new CellImpl(50, 50));
        cells.add(new CellImpl(100, 15));
        cells.add(new CellImpl(10, 5000));

        ATM atm2 = new ATMImpl(cells);
        System.out.println("Initial balance atm2: " + atm2.getBalance());

        bank.addATM(atm1);
        bank.addATM(atm2);
        System.out.println("Initial balance bank: " + bank.getBalance());

        atm1.withdraw(1565);
        atm2.withdraw(1280);

        System.out.println("Final balance atm1: " + atm1.getBalance());
        System.out.println("Final balance atm2: " + atm2.getBalance());
        System.out.println("Final balance bank: " + bank.getBalance());
        bank.downloadATMs();

        System.out.println("Balance bank after atm downloading: " + bank.getBalance());
    }
}
