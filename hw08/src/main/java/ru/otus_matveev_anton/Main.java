package ru.otus_matveev_anton;

import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import ru.otus_matveev_anton.entity.Characteristic;
import ru.otus_matveev_anton.entity.ColorCharacteristic;
import ru.otus_matveev_anton.entity.Product;
import ru.otus_matveev_anton.entity.WeightCharacteristic;
import ru.otus_matveev_anton.myjson.JsonFactory;
import ru.otus_matveev_anton.myjson.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

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
        System.out.println("gson with default settings skips nulls, gets field names by getters, throw StackOverflowError for cyclic links:");
        System.out.println(gson.toJson(product1));
        System.out.println("---------");

        ObjectMapper mapper = new ObjectMapper();
        System.out.println("jackson with default settings gets fields by class fields, throw StackOverflowError for cyclic links:");
        System.out.println(mapper.writeValueAsString(product1));
        System.out.println("---------");

        System.out.println("myJson:");
        JsonWriter jsonWriter = new JsonFactory().buildWriter(JsonFactory.FieldWritingMode.Getters, JsonFactory.CyclicLinksWritingMode.Skip, JsonFactory.SKIP_NULLS);
        System.out.println(jsonWriter.toJson(product1));

        System.out.println("myJson with cyclic links:");

        product1.setParts(Collections.singletonList(product1));
        product1.setParent(product1);
        System.out.println(jsonWriter.toJson(product1));
    }
}
