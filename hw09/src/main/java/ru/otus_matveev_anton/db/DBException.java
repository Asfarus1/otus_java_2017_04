package ru.otus_matveev_anton.db;

/**
 * Created by asfarus on 06.06.17.
 */
public class DBException extends RuntimeException{
    public DBException(Throwable throwable) {
        super(throwable);
    }
}
