package ru.otus_matveev_anton.myjson;

import java.util.Objects;

public class MyJsonBuilder {

    public enum FieldWritingMode{
        Getters,
        Fields
    }

    public enum CyclicLinksWritingMode {
        Skip,
        ThrowException,
        NotLookFor
    }

    private FieldWritingMode fieldWritingMode = FieldWritingMode.Getters;
    private CyclicLinksWritingMode cyclicLinksWritingMode = CyclicLinksWritingMode.Skip;
    private boolean isSkipNullFields;

    public FieldWritingMode getFieldWritingMode() {
        return fieldWritingMode;
    }

    public MyJsonBuilder setFieldWritingMode(FieldWritingMode fieldWritingMode) {
        Objects.requireNonNull(fieldWritingMode);
        this.fieldWritingMode = fieldWritingMode;
        return this;
    }

    public CyclicLinksWritingMode getCyclicLinksWritingMode() {
        return cyclicLinksWritingMode;
    }

    public MyJsonBuilder setCyclicLinksWritingMode(CyclicLinksWritingMode cyclicLinksWritingMode) {
        Objects.requireNonNull(fieldWritingMode);
        this.cyclicLinksWritingMode = cyclicLinksWritingMode;
        return this;
    }

    public boolean isSkipNullFields() {
        return isSkipNullFields;
    }

    public MyJsonBuilder setSkipNullFields(boolean skipNullFields) {
        isSkipNullFields = skipNullFields;
        return this;
    }

    public JsonWriter buildWriter(){
        JsonWritingFuncContainer myJson = new JsonWritingWriterImpl();
        myJson.setSkipNullFields(isSkipNullFields);

        ObjectWritingAlgorithm algorithm = null;
        switch (fieldWritingMode){
            case Fields:
                throw new IllegalArgumentException();
//                break;
            case Getters:
                algorithm = new FunctionForWritingByGetters(myJson);
                break;
        }
        myJson.setObjectWritingAlgorithm(algorithm);

        ObjectLinksWalker walker = null;
        switch (cyclicLinksWritingMode) {
            case NotLookFor:
                walker = (obj, links) -> (stringBuilder, o) -> {
                };
                break;
            case Skip:
                walker = (obj, links) ->
                        (stringBuilder, o) -> {
                            if (links.contains(obj)) {
                                return;
                            }
                            links.add(obj);
                        };
                break;
            case ThrowException:
                walker = (obj, links) ->
                    (stringBuilder, o) -> {
                        if (links.contains(obj)) {
                            throw new RuntimeException("cyclic link " + o);
                        }
                        links.add(obj);
                    };
                break;
        }
        myJson.setObjectLinksWalker(walker);
        return myJson;
    }
}
