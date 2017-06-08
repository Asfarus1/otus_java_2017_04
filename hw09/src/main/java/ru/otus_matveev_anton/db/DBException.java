package ru.otus_matveev_anton.db;

public class DBException extends RuntimeException{
    public DBException(Throwable throwable) {
        super(throwable);
    }
}
