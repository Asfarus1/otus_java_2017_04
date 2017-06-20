package ru.otus_matveev_anton.db.data_sets;

import ru.otus_matveev_anton.db.my_orm.DataSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

@Entity
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

    @Override
    public String
    toString() {
        return "UserDataSet{" +
                "id='" + getId() + '\'' +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDataSet that = (UserDataSet) o;

        return getId() == that.getId() && getAge() == that.getAge() && (getName() != null ? getName().equals(that.getName()) : that.getName() == null);
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + getAge();
        result = 31 * result + Long.hashCode(getId());
        return result;
    }

    public UserDataSet(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public UserDataSet() {
    }
}
