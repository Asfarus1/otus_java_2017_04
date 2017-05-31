package ru.otus_matveev_anton.entity;

/**
 * Created by Matveev.AV1 on 31.05.2017.
 */
public class WeightCharacteristic extends Characteristic<Integer> {
    public WeightCharacteristic() {
        super();
    }

    public WeightCharacteristic(Integer value) {
        this();
        setValue(value);
    }

    @Override
    public String getTitle() {
        return "weight";
    }
}
