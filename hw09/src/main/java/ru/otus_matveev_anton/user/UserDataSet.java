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
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
