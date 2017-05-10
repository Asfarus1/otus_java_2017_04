package ru.otus_matveev_anton.hw05.testpackage;

import ru.otus_matveev_anton.hw05.testframework.anotations.Before;
import ru.otus_matveev_anton.hw05.testframework.anotations.Ignore;
import ru.otus_matveev_anton.hw05.testframework.anotations.Test;
import ru.otus_matveev_anton.hw05.testframework.utils.Assert;

import java.util.*;

public class MyListTest {

    private List<Integer> myList;

    @Before
    public void setUp() throws Exception {
        myList = new ArrayList<>();
        Collections.addAll(myList,9,8,7,6,5,4,3,2,1);
    }

    @Test
    public void testSize() throws Exception {
        Assert.assertEquals(myList.size(),9);
    }

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertFalse(myList.isEmpty());
        myList.clear();
        Assert.assertTrue(myList.isEmpty());
    }

    @Test
    public void testContains() throws Exception {
        for (int i = 1; i < 10; i++) {
            Assert.assertTrue(myList.contains(i));
        }
        Assert.assertFalse(myList.contains(0));
        Assert.assertFalse(myList.contains(10));
    }

    @Test
    public void testToArray() throws Exception {
        Assert.assertArrayEquals(new Integer[]{9,8,7,6,5,4,3,2,1}, myList.toArray());
    }

    @Test
    public void testToArrayWithArrayArg() throws Exception {
        Assert.assertArrayEquals(new Integer[]{9,8,7,6,5,4,3,2,1}, myList.toArray(new Integer[3]));
        Assert.assertArrayEquals(new Integer[]{9,8,7,6,5,4,3,2,1, null, null}, myList.toArray(new Integer[11]));
    }

    @Test
    public void testAdd() throws Exception {
        myList.add(8);
        Assert.assertArrayEquals(new Integer[]{9,8,7,6,5,4,3,2,1,8},myList.toArray());
    }

    @Test
    public void testAddOnPosition() throws Exception {
        myList.add(0,1);
        Assert.assertArrayEquals(new Integer[]{1,9,8,7,6,5,4,3,2,1},myList.toArray());
        myList.add(1,3);
        Assert.assertArrayEquals(new Integer[]{1,3,9,8,7,6,5,4,3,2,1},myList.toArray());
        myList.add(11,55);
        Assert.assertArrayEquals(new Integer[]{1,3,9,8,7,6,5,4,3,2,1,55},myList.toArray());
    }

    @Test
    public void testClear() throws Exception {
        myList.clear();
        Assert.assertArrayEquals(new Integer[0], myList.toArray());
    }

    @Test
    public void testGet() throws Exception {
        Assert.assertEquals(Integer.valueOf(3), myList.get(6));
    }

    @Test
    public void testSet() throws Exception {
        Assert.assertEquals(myList.set(2,1),Integer.valueOf(7));
        Assert.assertEquals(myList.set(5,1),Integer.valueOf(4));
        Assert.assertArrayEquals(new Integer[]{9,8,1,6,5,1,3,2,1},myList.toArray());
    }

    @Test
    public void testRemoveByIndex() throws Exception {
        Assert.assertEquals(myList.remove(0), Integer.valueOf(9));
        Assert.assertEquals(myList.remove(4),Integer.valueOf(4));
        Assert.assertArrayEquals(new Integer[]{8,7,6,5,3,2,1},myList.toArray());
    }

    @Test
    public void testRemoveByValue() throws Exception {
        Assert.assertFalse(myList.remove(Integer.valueOf(0)));
        Assert.assertTrue(myList.remove(Integer.valueOf(4)));
        Assert.assertArrayEquals(new Integer[]{9,8,7,6,5,3,2,1},myList.toArray());
    }

    @Test
    public void testIndexOf() throws Exception {
        Assert.assertEquals(myList.indexOf(0), -1);
        Assert.assertEquals(myList.indexOf(1), 8);
        myList.add(1);
        Assert.assertEquals(myList.indexOf(1), 8);
    }

    @Test
    public void testLastIndexOf() throws Exception {
        Assert.assertEquals(myList.lastIndexOf(0), -1);
        Assert.assertEquals(myList.lastIndexOf(1), 8);
        myList.add(1);
        Assert.assertEquals(myList.lastIndexOf(1), 9);
    }

    @Test
    public void testContainsAll() throws Exception {
        Assert.assertTrue(myList.containsAll(Arrays.asList(5,8,4,1)));
        Assert.assertFalse(myList.containsAll(Arrays.asList(5,8,4,1,10)));
    }

    @Test
    public void testAddAll() throws Exception {
        Assert.assertTrue(myList.addAll(Arrays.asList(5,8,4,1)));
        Assert.assertArrayEquals(new Integer[]{9,8,7,6,5,4,3,2,1,5,8,4,1},myList.toArray());
    }

    @Test
    public void testAddAllOnPosition() throws Exception {
        Assert.assertTrue(myList.addAll(2,Arrays.asList(5,8,4,1)));
        Assert.assertArrayEquals(new Integer[]{9,8,5,8,4,1,7,6,5,4,3,2,1},myList.toArray());
    }

    @Test
    public void testRemoveAll() throws Exception {
        Assert.assertTrue(myList.removeAll(Arrays.asList(5,8,4,1)));
        Assert.assertArrayEquals(new Integer[]{9,7,6,3,2},myList.toArray());
    }

    @Test
    public void testRetainAll() throws Exception {
        Assert.assertTrue(myList.retainAll(Arrays.asList(5,8,4,1,12)));
        Assert.assertFalse(myList.retainAll(Arrays.asList(9,8,7,6,5,4,3,2,1,12)));
        Assert.assertArrayEquals(new Integer[]{8,5,4,1},myList.toArray());
    }

    @Test
    public void testRemoveIf() throws Exception {
        Assert.assertTrue(myList.removeIf(i->i%2==0));
        Assert.assertFalse(myList.removeIf(i->i>10));
        Assert.assertArrayEquals(new Integer[]{9,7,5,3,1},myList.toArray());
    }

    @Test
    public void testIterator() throws Exception {
        Iterator<Integer> iter = myList.iterator();

        for (int i = 9; i > 0; i--) {
             Assert.assertTrue(iter.hasNext());
            Assert.assertEquals(Integer.valueOf(i), iter.next());
            if (i % 2 == 0){
                iter.remove();
            }
        }
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void testListIterator() throws Exception {
        ListIterator<Integer> iter = myList.listIterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertFalse(iter.hasPrevious());
        Assert.assertEquals(Integer.valueOf(9), iter.next());
        Assert.assertTrue(iter.hasPrevious());
        Assert.assertEquals(Integer.valueOf(8), iter.next());
        Assert.assertEquals(Integer.valueOf(8), iter.previous());
        Assert.assertEquals(Integer.valueOf(9), iter.previous());
    }

    @Test
    public void testListIteratorFromPos() throws Exception {
        ListIterator<Integer> iter = myList.listIterator(8);
        Assert.assertTrue(iter.hasNext());
        Assert.assertTrue(iter.hasPrevious());
        Assert.assertEquals(Integer.valueOf(2), iter.previous());
    }

    @Test
    @Ignore
    public void testSubList() throws Exception {

    }

    @Test
    public void CollectionsSort(){
        Collections.sort(myList,Integer::compare);
        Assert.assertArrayEquals(new Integer[]{1,2,3,4,5,6,7,8,9}, myList.toArray());
    }

    @Test
    public void CollectionsCopy(){
        List<Integer> newList = new ArrayList<>();
        Collections.addAll(newList, new Integer[9]);
        Collections.copy(newList, myList);
        Assert.assertArrayEquals(new Integer[]{9,8,7,6,5,4,3,2,1}, newList.toArray());
    }

}