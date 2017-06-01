package ru.otus_matveev_anton.myjson;

@FunctionalInterface
public interface ObjectWritingAlgorithm {
    FunctionPart getFunctionalForParse(Class clazz, boolean isSkipNullFields);
}
