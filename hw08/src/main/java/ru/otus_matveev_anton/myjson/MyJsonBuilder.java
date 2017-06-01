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

        ObjectWritingAlgorithm algorithm;
        switch (fieldWritingMode){
            case Fields:
                throw new IllegalArgumentException();
//                break;
            case Getters:
                algorithm = new FunctionForWritingByGetters(myJson);
                break;
            default:
                throw new IllegalArgumentException();
        }
        myJson.setObjectWritingAlgorithm(algorithm);
        myJson.setCyclicLinksWritingMode(cyclicLinksWritingMode);
        return myJson;
    }
}
