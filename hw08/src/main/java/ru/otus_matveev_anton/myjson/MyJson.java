package ru.otus_matveev_anton.myjson;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Matveev.AV1 on 31.05.2017.
 */
public class MyJson {
    Map<Class,SoftReference<ToJson>> mapToJson = new ConcurrentHashMap<>();
    Map<Class,SoftReference<FromJson>> mapFromJson = new ConcurrentHashMap<>();

    private ToJson getFuncToJson(Class<?> clazz){
        SoftReference softFunc = mapToJson.get(clazz);
        ToJson func = null;
        if (softFunc != null){
            func = (ToJson) softFunc.get();
        }

        if (func == null){

        }
        return func;
    }

    private ToJson createFuncToJson(Class<?> clazz){
        ToJson func = null;
        return func;
    }
}
