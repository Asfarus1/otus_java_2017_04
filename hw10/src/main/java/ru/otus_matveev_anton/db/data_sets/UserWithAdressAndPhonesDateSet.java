package ru.otus_matveev_anton.db.data_sets;

import javax.persistence.*;
import java.util.Set;

@Entity
///не смог понять как переписать имя таблицы наследованное от родителя
@Table(name = "users_with_adress_and_phones")
public class UserWithAdressAndPhonesDateSet extends UserDataSet {

    @OneToOne(cascade = CascadeType.ALL,targetEntity = AdressDataSet.class, mappedBy = "user", orphanRemoval = true)
    private AdressDataSet adress;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = PhoneDataSet.class, mappedBy = "user", orphanRemoval = true)
    private Set<PhoneDataSet> phones;

    public UserWithAdressAndPhonesDateSet(String name, int age, AdressDataSet adress, Set<PhoneDataSet> phones) {
        super(name, age);
        this.adress = adress;
        adress.setUser(this);
        this.phones = phones;
        phones.forEach((p)->p.setUser(this));
    }

    public UserWithAdressAndPhonesDateSet() {
        super();
    }

    public AdressDataSet getAdress() {
        return adress;
    }

    public void setAdress(AdressDataSet adress) {
        this.adress = adress;
    }

    public Set<PhoneDataSet> getPhones() {
        return phones;
    }

    public void setPhones(Set<PhoneDataSet> phones) {
        this.phones = phones;
    }

    @Override
    public String toString() {
        return "UserWithAdressAndPhonesDateSet{" +
                "id=" + getId() +
                ",age=" + getAge() +
                ",name=" + getName() +
                ",adress=" + adress +
                ", phones=" + phones +
                '}';
    }
}
