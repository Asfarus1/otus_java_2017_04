package ru.otus_matveev_anton.atm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class ATMImplTest {
    private ATM atm;
    private Cell[] cells;

    @Before
    public void setUp() throws Exception {
        cells = new Cell[]{
                new CellImpl(100,5),
                new CellImpl(100,2)
        };
        atm = new ATMImpl(Arrays.asList(cells));
    }

    @Test
    public void getBalance() throws Exception {
        Assert.assertEquals(5*100 + 2*100, atm.getBalance());
    }

    @Test
    public void download() throws Exception {
        cells[0].withdraw(5);
        cells[1].withdraw(4);
        Assert.assertEquals(5*(100-5) + 2*(100-4), atm.getBalance());
        atm.download();
        Assert.assertEquals(5*100 + 2*100, atm.getBalance());
    }

    @Test
    public void withdraw() throws Exception {
        atm.setAlgorithm(new WithdrawAlgorithmMaxBigNominals());

        Assert.assertTrue(atm.withdraw(9));
        int balance = 5*(100-1) + 2*(100-2);
        Assert.assertEquals(balance, atm.getBalance());

        Assert.assertFalse(atm.withdraw(3));
        Assert.assertEquals(balance, atm.getBalance());

        Assert.assertFalse(atm.withdraw(702));
        Assert.assertEquals(balance, atm.getBalance());

        Assert.assertFalse(atm.withdraw(1));
        Assert.assertEquals(balance, atm.getBalance());

        Assert.assertTrue(atm.withdraw(2));
        balance -= 2;
        Assert.assertEquals(balance, atm.getBalance());
    }

}