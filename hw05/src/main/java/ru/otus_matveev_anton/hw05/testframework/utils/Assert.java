package ru.otus_matveev_anton.hw05.testframework.utils;

import java.util.Objects;

public final class Assert {
    protected Assert() {}

    public static void assertEquals(Object expected, Object actual){
        if (!Objects.equals(expected,actual)){
            fail(String.format("expected: %s, actual: %s",expected, actual));
        }
    }

    public static void assertTrue(boolean flag){
        if (!flag){
            fail("is not true");
        }
    }

    public static void assertFalse(boolean flag){
        if (flag){
            fail("is not false");
        }
    }

    public static void assertArrayEquals(Object[] expected, Object[] actual) {
        if (expected == actual) {
            return;
        }
        if (expected == null) {
            fail("expected array is null, but actual isn't");
        }
        if (actual == null) {
            fail("actual array is null, but expected isn't");
        }

        int length = expected.length;
        if (length != actual.length) {
            fail(String.format("expected array lenght = %d, actual array length = %d", length, actual.length));
        }

        for (int i = 0; i < length; i++) {
            Object o1 = expected[i];
            Object o2 = actual[i];
            if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                fail(String.format("elements with index %d are not equals! expected: %s, actual: %s", i, o1, o2));
            }
        }
    }

    public static void notNull(Object obj){
        if (obj == null){
            fail("Must not be null!");
        }
    }

    public static void fail(String msg){
        throw new AssertionError(msg);
    }

}
