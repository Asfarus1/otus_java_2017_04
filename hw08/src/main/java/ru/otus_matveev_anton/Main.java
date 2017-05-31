package ru.otus_matveev_anton;

import com.sun.xml.internal.ws.spi.db.PropertyAccessor;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.ObjectMapper;
import ru.otus_matveev_anton.entity.Characteristic;
import ru.otus_matveev_anton.entity.ColorCharacteristic;
import ru.otus_matveev_anton.entity.Product;
import ru.otus_matveev_anton.entity.WeightCharacteristic;
import com.google.gson.*;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        Product product1 = new Product();
        product1.setPrice(23);
        product1.setTitle("телевизор");
        product1.setVendor("");
        product1.setCharacteristics(Arrays.asList(new Characteristic[]{new WeightCharacteristic(12), new ColorCharacteristic("red")}));

        System.out.println("toString:");
        System.out.println(product1);
        System.out.println("---------");

        Gson gson = new Gson();
        System.out.println("gson with default settings:");
        System.out.println(gson.toJson(product1));
        System.out.println("---------");

        ObjectMapper mapper = new ObjectMapper();
        System.out.println("jackson with default settings:");
        System.out.println(mapper.writeValueAsString(product1));
        System.out.println("---------");
    }
}
