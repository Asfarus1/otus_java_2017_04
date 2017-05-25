package ru.otus_matveev_anton;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.otus_matveev_anton.atm.*;

import java.util.Arrays;
import java.util.Collections;

public class BankTest {
    private ATM[] atms;
    private Bank bank;

    @Before
    public void setUp() throws Exception {
        atms = new ATM[2];
        atms[0] = new ATMImpl(Collections.singletonList(new CellImpl(5,10)));
        atms[1] = new ATMImpl(Collections.singletonList(new CellImpl(2,10)));
        WithdrawAlgorithm alg = new WithdrawAlgorithmMaxBigNominals();
        Arrays.stream(atms).forEach(atm -> atm.setAlgorithm(alg));
        bank = new Bank();
        bank.addATM(atms[0]);
        bank.addATM(atms[1]);
    }

    @Test
    public void getBalance() throws Exception {
        Assert.assertEquals(atms[0].getBalance() + atms[1].getBalance(), bank.getBalance());
    }

    @Test
    public void downloadATMs() throws Exception {
        atms[0].withdraw(50);
        bank.downloadATMs();
        Assert.assertEquals(5*10 + 2*10, bank.getBalance());
    }

    @Test
    public void addATM() throws Exception {
        bank.addATM(new ATMImpl(Collections.singletonList(new CellImpl(1,1))));
        Assert.assertEquals(5*10 + 2*10 + 1, bank.getBalance());
    }

    @Test
    public void removeATM() throws Exception {
        bank.removeATM(atms[0]);
        Assert.assertEquals(2*10, bank.getBalance());
    }

}