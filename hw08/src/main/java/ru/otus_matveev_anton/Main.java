package ru.otus_matveev_anton;

import ru.otus_matveev_anton.entity.Characteristic;
import ru.otus_matveev_anton.entity.ColorCharacteristic;
import ru.otus_matveev_anton.entity.Product;
import ru.otus_matveev_anton.entity.WeightCharacteristic;
import ru.otus_matveev_anton.myjson.MyJson;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        Product product1 = new Product();
        product1.setPrice(23);
        product1.setTitle("телевизор");
        product1.setVendor("");
        product1.setCharacteristics(Arrays.asList(new Characteristic[]{new WeightCharacteristic(12), new ColorCharacteristic("red")}));

//        System.out.println("toString:");
//        System.out.println(product1);
//        System.out.println("---------");
//
//        Gson gson = new Gson();
//        System.out.println("gson with default settings:");
//        System.out.println(gson.toJson(product1));
//        System.out.println("---------");
//
//        ObjectMapper mapper = new ObjectMapper();
//        System.out.println("jackson with default settings:");
//        System.out.println(mapper.writeValueAsString(product1));
//        System.out.println("---------");

        MyJson myJson = new MyJson();
        System.out.println("myJson:");
//        System.out.println(myJson.toJson(Arrays.asList(new String[]{"11111","222222"})));
//        System.out.println(myJson.toJson(new String[][]{{"11111","222222"},{"fdf"}}));
        System.out.println(myJson.toJson(new ColorCharacteristic("red")));
        System.out.println(myJson.toJson(product1));
        System.out.println(myJson.toJson(product1));
        System.out.println(myJson.toJson(product1));
        System.out.println(myJson.toJson(product1));
        System.out.println("---------");

    }
}
