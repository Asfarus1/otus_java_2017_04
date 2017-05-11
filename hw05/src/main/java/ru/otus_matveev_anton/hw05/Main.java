package ru.otus_matveev_anton.hw05;

import ru.otus_matveev_anton.hw05.testframework.TestHelper;
import ru.otus_matveev_anton.hw05.testpackage.MyListTest;

public class Main {
    public static void main(String[] args) throws Exception {
        String packageName = "ru.otus_matveev_anton.hw05";

        System.out.println("--In package-----------");
        TestHelper.executeTestsInPackage(packageName);
        System.out.println("-----------------------");
        System.out.println("--In class-------------");
        TestHelper.executeTests(MyListTest.class);
    }

}
