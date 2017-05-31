package ru.otus_matveev_anton.entity;

public abstract class Characteristic<T> {
    private  T value;

    public Characteristic() {
    }

    public String getTitle() {
        return "";
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Characteristic{" +
                "title='" + getTitle() + '\'' +
                ", value=" + value +
                '}';
    }
}
