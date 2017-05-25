package ru.otus_matveev_anton.atm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CellImplTest {

    private Cell cell;

    @Before
    public void setUp() throws Exception {
     cell = new CellImpl(100,5);
    }
    @Test
    public void getNominal() throws Exception {
        Assert.assertEquals(5, cell.getNominal());
    }

    @Test
    public void getCount() throws Exception {
        Assert.assertEquals(100, cell.getCount());
        cell.withdraw(4);
        Assert.assertEquals(100-4, cell.getCount());
    }

    @Test
    public void download() throws Exception {
        cell.withdraw(4);
        Assert.assertEquals(100-4, cell.getCount());
        cell.download();
        Assert.assertEquals(100, cell.getCount());
    }

    @Test
    public void withdraw() throws Exception {
        cell.withdraw(4);
        Assert.assertEquals(100-4, cell.getCount());
    }
}