package ru.otus_matveev_anton.myjson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AlgorithmForWritingByGetters implements ObjectWritingAlgorithm {
    private JsonWritingFuncProvider funcProvider;
    private FieldFilter fieldFilter;

    public void setFieldFilter(FieldFilter fieldFilter) {
        this.fieldFilter = fieldFilter;
    }

    public void setFuncProvider(JsonWritingFuncProvider funcProvider) {
        this.funcProvider = funcProvider;
    }

    @Override
    public FunctionPart getFunctionForWritingObject(Class clazz) {
        FunctionPart func = (sb, v, set) -> sb.append('{');

        Method[] methods = clazz.getMethods();

        String mName;
        for (Method method : methods) {
            mName = method.getName();
            if (method.getParameterCount() != 0) {
                continue;
            } else if (mName.startsWith("get") && !mName.equals("getClass")) {
                mName = mName.substring(3, 4).toLowerCase().concat(mName.substring(4));
            } else if (mName.startsWith("is")) {
                mName = mName.substring(2, 3).toLowerCase().concat(mName.substring(3));
            } else {
                continue;
            }
            final String fieldName = mName;

            func = func.andThen((sb, v, set) -> {
                Object field;
                try {
                    field = method.invoke(v);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(String.format("%s.%s: can't get value of field", clazz.getCanonicalName(), fieldName), e);
                }
                if (fieldFilter.test(fieldName, field, set)) {
                    sb.append('"')
                            .append(fieldName)
                            .append("\":");
                    funcProvider.getFuncToJson(field).accept(sb, field, set);
                    sb.append(',');
                }
            });
        }
        return func.andThen((sb, v, set) -> sb.setCharAt(sb.length() - 1, '}'));
    }
}
