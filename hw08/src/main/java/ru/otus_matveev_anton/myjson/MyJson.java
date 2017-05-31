package ru.otus_matveev_anton.myjson;

import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Matveev.AV1 on 31.05.2017.
 */
public class MyJson {
    private Map<Class, SoftReference<FunctionPart>> mapToJson = new ConcurrentHashMap<>();
    private Map<Class, SoftReference<FunctionPart>> mapFromJson = new ConcurrentHashMap<>();

    private final FunctionPart nullFunc = (sb,v)->sb.append("null");

    public String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        FunctionPart func = getFuncToJson(obj);
        func.accept(sb, obj);
        return sb.toString();
    }

    private FunctionPart getFuncToJson(Object obj) {

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
            func = getStandartFuncToJson(obj);
            mapToJson.putIfAbsent(clazz, new SoftReference<>(func));
        }
        return func;
    }

    private FunctionPart getStandartFuncToJson(Object obj) {
        if (obj == null) {
            return (sb, v) -> sb.append("null");
        } else if (obj instanceof CharSequence || obj instanceof Character) {
            return (sb, v) -> sb.append('"').append(v).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return (sb, v) -> sb.append(v);
        } else if (obj instanceof Iterable) {
            return (sb, v) -> iterate(sb, (Iterable) v);
        } else if (obj instanceof Object[]) {
            return (sb, v) -> iterate(sb, (Object[]) (v));
        }

        Class clazz = obj.getClass();
        FunctionPart func = (sb, v) -> {
            sb.append('{');
        };

        Method[] methods = clazz.getMethods();

        String mName;
        for (Method method : methods) {
            mName = method.getName();

            if (mName.startsWith("get") && method.getParameterCount() == 0 && !mName.equals("getClass")) {
                final String fieldName = mName.substring(2, 3).toLowerCase().concat(mName.substring(4));

                func = func.andThen((sb, v) -> {
                    Object field = null;
                    try {
                        field = method.invoke(v);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(String.format("$s.%s: can't get value of field", clazz.getCanonicalName(), fieldName), e);
                    }
                    sb.append('"')
                            .append(fieldName)
                            .append("\":");
                    getFuncToJson(field).accept(sb, field);
                    sb.append(',');

                });
            }
        }
        return func.andThen((sb, v) -> sb.setCharAt(sb.length() - 1, '}'));
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
