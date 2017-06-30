package ru.otus_matveev_anton;

import ru.otus_matveev_anton.db.DBService;
import ru.otus_matveev_anton.db.DBServiceMyOrmImpl;
import ru.otus_matveev_anton.db.DBServiceWithCache;
import ru.otus_matveev_anton.db.data_sets.UserDataSet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;


public class MainMyOrmWithCache {

    private Random rnd = new Random();
    private AtomicLong seq = new AtomicLong(0);

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
        new MainMyOrmWithCache().runTest();
    }

    private void runTest() throws InterruptedException {
        DBService dbService = new DBServiceWithCache(new DBServiceMyOrmImpl());
        UserDataSet user;
        try {
            while (true){
                user = getRandomUser();
                dbService.saveUser(user);
                Thread.sleep(rnd.nextInt(2_000));
                user = dbService.getUser(rnd.nextInt(seq.intValue() * 3));
                System.out.println("getted user " + user);
                Thread.sleep(rnd.nextInt(2_000));
            }
        }finally {
            dbService.shutdown();
        }

    }

    private UserDataSet getRandomUser(){
        UserDataSet user = new UserDataSet();
        user.setAge(rnd.nextInt(100));
        user.setName("user" + seq.incrementAndGet());
        return user;
    }
}
