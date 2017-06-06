package ru.otus_matveev_anton.user;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "users")
public class UserDataSet {

    @Id
    @Column(name = "id", length = 20)
    //long -длина 19
    private BigInteger id;

    @Column(name = "name")
    private String name;

    @Column(name = "age", length = 3, nullable = false)
    private short age;


    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

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
