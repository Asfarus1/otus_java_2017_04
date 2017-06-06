package ru.otus_matveev_anton.user;

import ru.otus_matveev_anton.db.DataSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class UserDataSet extends DataSet{

    @Column(name = "name")
    private String name;

    @Column(name = "age", length = 3, nullable = false)
    private short age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getAge() {
        return age;
    }

    public void setAge(short age) {
        this.age = age;
    }
}
