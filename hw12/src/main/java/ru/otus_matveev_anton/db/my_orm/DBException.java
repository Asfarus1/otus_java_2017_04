package ru.otus_matveev_anton.db.my_orm;

class DBException extends RuntimeException{
    public DBException(Throwable throwable) {
        super(throwable);
    }
}
