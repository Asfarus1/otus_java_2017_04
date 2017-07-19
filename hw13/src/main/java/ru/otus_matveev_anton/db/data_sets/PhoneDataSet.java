package ru.otus_matveev_anton.db.data_sets;

import ru.otus_matveev_anton.db.my_orm.DataSet;

import javax.persistence.*;

@Entity
@Table(name = "phones")
public class PhoneDataSet extends DataSet {

    @Column(name = "code")
    private int code;

    @Column(name = "number")
    private String number;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserWithAdressAndPhonesDateSet user;

    public PhoneDataSet() {
    }

    public PhoneDataSet(String number) {
        this.number = number;
    }

    public UserWithAdressAndPhonesDateSet getUser() {
        return user;
    }

    public void setUser(UserWithAdressAndPhonesDateSet user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "PhoneDataSet{" +
                "id=" + getId() +
                ",code=" + code +
                ", number='" + number + '\'' +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
