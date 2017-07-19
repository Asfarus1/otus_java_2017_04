package ru.otus_matveev_anton.db.data_sets;

import ru.otus_matveev_anton.db.my_orm.DataSet;

import javax.persistence.*;

@Entity
@Table(name = "adresses")
public class AdressDataSet extends DataSet {

    @Column(name = "street")
    private String street;

    @Column(name= "index")
    private int index;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserWithAdressAndPhonesDateSet user;

    public AdressDataSet(String street, int index) {
        this.street = street;
        this.index = index;
    }

    public AdressDataSet() {
    }

    public UserWithAdressAndPhonesDateSet getUser() {
        return user;
    }

    public void setUser(UserWithAdressAndPhonesDateSet user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "AdressDataSet{" +
                "id=" + getId() +
                ",street='" + street + '\'' +
                ", index=" + index +
                '}';
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
