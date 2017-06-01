package ru.otus_matveev_anton.myjson;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class JsonWritingWriterImpl implements JsonWritingFuncProvider,JsonWriter{
    private final static FunctionPart nullFunc = (sb,v, set)->sb.append("null");
    private Map<Class, SoftReference<FunctionPart>> mapToJson = new ConcurrentHashMap<>();
    private ObjectWritingAlgorithm objectWritingAlgorithm;
    private FieldFilter filter;

    JsonWritingWriterImpl() {

    }

    public void setObjectWritingAlgorithm(ObjectWritingAlgorithm objectWritingAlgorithm) {
        this.objectWritingAlgorithm = objectWritingAlgorithm;
    }

    public void setFieldFilter(FieldFilter filter) {
        this.filter = filter;
    }

    public String toJson(Object obj) {
        Set<Object> objectLinks = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        FunctionPart func = getFuncToJson(obj);
        func.accept(sb, obj, objectLinks);
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
            return (sb, v, set) -> sb.append('"').append(v).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return (sb, v, set) -> sb.append(v);
        } else if (obj instanceof Iterable) {
            return wrapLinkWalker((sb, v, set) -> iterate(sb, (Iterable) v, set));
        } else if (obj instanceof Object[]) {
            return wrapLinkWalker((sb, v, set) -> iterate(sb, (Object[]) (v), set));
        }

        return wrapLinkWalker(objectWritingAlgorithm.getFunctionForWritingObject(obj.getClass()));
    }

    private void iterate(StringBuilder sb, Iterable obj, Set<Object> objectLinks) {
        sb.append('[');
        for (Object elem : obj) {
            this.getFuncToJson(elem).accept(sb, elem, objectLinks);
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
    }

    private void iterate(StringBuilder sb, Object[] obj, Set<Object> objectLinks) {
        sb.append('[');
        for (Object elem : obj) {
            this.getFuncToJson(elem).accept(sb, elem, objectLinks);
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ']');
    }

    private FunctionPart wrapLinkWalker(FunctionPart func){
        return (sb, v, set)->{
            if (filter.test(null, v, set)) {
                set.add(v);
                func.accept(sb, v, set);
                set.remove(v);
            }
        };
    }
}
