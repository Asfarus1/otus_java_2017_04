package ru.otus_matveev_anton.entity;

public class ColorCharacteristic extends Characteristic<String> {
    public ColorCharacteristic() {
        super();
    }

    public ColorCharacteristic(String value) {
        this();
        setValue(value);
    }

    @Override
    public String getTitle() {
        return "color";
    }
}
