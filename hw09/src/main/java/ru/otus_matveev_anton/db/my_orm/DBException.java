package ru.otus_matveev_anton.db.my_orm;

public class DBException extends RuntimeException{
    DBException(Throwable throwable) {
        super(throwable);
    }
}
