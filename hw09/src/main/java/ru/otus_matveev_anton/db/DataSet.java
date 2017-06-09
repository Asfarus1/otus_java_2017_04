package ru.otus_matveev_anton.db;


import javax.persistence.Column;
import javax.persistence.Id;

public class DataSet {
    @Id
    @Column(name = "id")
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
