package ru.otus_matveev_anton.myjson;

interface JsonWritingFuncProvider extends JsonWriter{

    FunctionPart getFuncToJson(Object obj);

    void setObjectWritingAlgorithm(ObjectWritingAlgorithm alg);

    void setFieldFilter(FieldFilter filter);
}
