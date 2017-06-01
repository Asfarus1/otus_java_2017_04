package ru.otus_matveev_anton.entity;

import java.util.List;

/**
 * Created by Matveev.AV1 on 31.05.2017.
 */
public class Product {
    private Long id;
    private String title;
    private int price;
    private String vendor;
    transient private int lastPrice;
    private List<Characteristic> characteristics;
    private List<Product> parts;

//    public String getVendor() {
//        return vendor;
//    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public int getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(int lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.lastPrice = this.price;
        this.price = price;
    }

    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<Characteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public List<Product> getParts() {
        return parts;
    }

    public void setParts(List<Product> parts) {
        this.parts = parts;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", vendor='" + vendor + '\'' +
                ", lastPrice=" + lastPrice +
                ", characteristics=" + characteristics +
                '}';
    }
}
