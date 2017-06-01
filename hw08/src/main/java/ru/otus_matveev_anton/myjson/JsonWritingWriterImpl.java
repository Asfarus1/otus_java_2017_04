package ru.otus_matveev_anton.myjson;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class JsonWritingWriterImpl implements JsonWritingFuncContainer,JsonWriter{
    private Map<Class, SoftReference<FunctionPart>> mapToJson = new ConcurrentHashMap<>();
    private final static FunctionPart nullFunc = (sb,v)->sb.append("null");
    private ObjectWritingAlgorithm objectWritingAlgorithm;
    private boolean isSkipNullFields;
    private ThreadLocal<Set<Object>> objectLinks = ThreadLocal.withInitial(HashSet<Object>::new);
    private MyJsonBuilder.CyclicLinksWritingMode cyclicLinksWritingMode;

    JsonWritingWriterImpl() {

    }

    @Override
    public void setCyclicLinksWritingMode(MyJsonBuilder.CyclicLinksWritingMode mode) {
        this.cyclicLinksWritingMode = mode;
    }

    public void setObjectWritingAlgorithm(ObjectWritingAlgorithm objectWritingAlgorithm) {
        this.objectWritingAlgorithm = objectWritingAlgorithm;
    }

    public void setSkipNullFields(boolean skipNullFields) {
        isSkipNullFields = skipNullFields;
    }

    public String toJson(Object obj) {
        Set<Object> addedObjects = objectLinks.get();
        if(cyclicLinksWritingMode == MyJsonBuilder.CyclicLinksWritingMode.Skip){

        }
        StringBuilder sb = new StringBuilder();
        FunctionPart func = getFuncToJson(obj);
        func.accept(sb, obj);
        return sb.toString();
    }

    public FunctionPart getFuncToJson(Object obj) {

        if (obj == null){
            return nullFunc;
        }

        Class clazz = obj.getClass();

        SoftReference softFunc = mapToJson.get(clazz);
        FunctionPart func = null;
        if (softFunc != null) {
            func = (FunctionPart) softFunc.get();
        }

        if (func == null) {
            func = getStandardFuncToJson(obj);
            mapToJson.putIfAbsent(clazz, new SoftReference<>(func));
        }
        return func;
    }

    private FunctionPart getStandardFuncToJson(Object obj) {
        if (obj instanceof CharSequence || obj instanceof Character) {
            return (sb, v) -> sb.append('"').append(v).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return StringBuilder::append;
        } else if (obj instanceof Iterable) {
            return (sb, v) -> iterate(sb, (Iterable) v);
        } else if (obj instanceof Object[]) {
            return (sb, v) -> iterate(sb, (Object[]) (v));
        }

        return objectWritingAlgorithm.getFunctionalForParse(obj.getClass(), isSkipNullFields);
    }

    private void iterate(StringBuilder sb, Iterable obj) {
        sb.append('[');
        for (Object elem : obj) {
            this.getFuncToJson(elem).accept(sb, elem);
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
    }

    private void iterate(StringBuilder sb, Object[] obj) {
        sb.append('[');
        for (Object elem : obj) {
            this.getFuncToJson(elem).accept(sb, elem);
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
    }
}
