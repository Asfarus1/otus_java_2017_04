package ru.otus_matveev_anton.myjson;

import java.util.Objects;

public class JsonFactory {

    public enum FieldWritingMode {
        Getters,
        Fields
    }

    public enum CyclicLinksWritingMode {
        Skip,
        ThrowException
    }

    public final static FieldFilter SKIP_NULLS = (fieldName, value, objectLinks) -> value != null;

    private FieldFilter fieldFilter;


    private FieldFilter getFilter(CyclicLinksWritingMode cyclicLinksWritingMode) {
        Objects.requireNonNull(cyclicLinksWritingMode);
        switch (cyclicLinksWritingMode) {
            case Skip:
                return (fieldName, value, objectLinks) -> !objectLinks.contains(value);
            default:
                return (fieldName, value, objectLinks) -> {
                    if (objectLinks.contains(value)) {
                        throw new RuntimeException("cyclic link " + value);
                    }
                    return true;
                };
        }
    }

    private ObjectWritingAlgorithm getAlgorithm(FieldWritingMode fieldWritingMode) {
        Objects.requireNonNull(fieldWritingMode);
        switch (fieldWritingMode) {
            case Getters:
                return new AlgorithmForWritingByGetters();
            default:
                throw new RuntimeException("Wrong fieldWritingMode^" + fieldWritingMode);
        }
    }

    public JsonWriter buildWriter(FieldWritingMode fieldWritingMode, CyclicLinksWritingMode cyclicLinksWritingMode, FieldFilter... filters) {
        FieldFilter filter = getFilter(cyclicLinksWritingMode);
        ObjectWritingAlgorithm alg = getAlgorithm(fieldWritingMode);
        return buildWriter(alg, filter, filters);
    }

    public JsonWriter buildWriter(FieldWritingMode fieldWritingMode, FieldFilter filter, FieldFilter... filters) {
        ObjectWritingAlgorithm alg = getAlgorithm(fieldWritingMode);
        return buildWriter(alg, filter, filters);
    }

    public JsonWriter buildWriter(ObjectWritingAlgorithm alg, CyclicLinksWritingMode cyclicLinksWritingMode, FieldFilter... filters) {
        FieldFilter filter = getFilter(cyclicLinksWritingMode);
        return buildWriter(alg, filter, filters);
    }

    public JsonWriter buildWriter(ObjectWritingAlgorithm alg, FieldFilter filter, FieldFilter... filters) {
        for (FieldFilter f : filters) {
            filter = filter.andThen(f);
        }
        alg.setFieldFilter(filter);
        JsonWritingFuncProvider myJson = new JsonWritingWriterImpl();
        myJson.setFieldFilter(filter);
        myJson.setObjectWritingAlgorithm(alg);
        alg.setFuncProvider(myJson);
        return myJson;
    }

}
