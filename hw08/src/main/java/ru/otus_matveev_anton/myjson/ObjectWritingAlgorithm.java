package ru.otus_matveev_anton.myjson;


public interface ObjectWritingAlgorithm {
    FunctionPart getFunctionForWritingObject(Class clazz);

    void setFieldFilter(FieldFilter fieldFilter);

    void setFuncProvider(JsonWritingFuncProvider funcProvider);
}
