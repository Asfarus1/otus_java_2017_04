package ru.otus_matveev_anton.myjson;

interface JsonWritingFuncContainer extends JsonWriter{
    FunctionPart getFuncToJson(Object obj);

    void setObjectWritingAlgorithm(ObjectWritingAlgorithm alg);

    void setSkipNullFields(boolean isSkipNullFields);

    void setObjectLinksWalker(ObjectLinksWalker linksWalker);
}
