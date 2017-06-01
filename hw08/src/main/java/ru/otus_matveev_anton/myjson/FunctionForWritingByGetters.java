package ru.otus_matveev_anton.myjson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class FunctionForWritingByGetters implements ObjectWritingAlgorithm {
    private JsonWritingFuncContainer myJson;

    FunctionForWritingByGetters(JsonWritingFuncContainer myJson) {
        this.myJson = myJson;
    }

    @Override
    public FunctionPart getFunctionalForParse(Class clazz, boolean isSkipNullFields) {
        FunctionPart func = (sb, v) -> {
            sb.append('{');
        };

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
            func = func.andThen((sb, v) -> {
                Object field = null;
                try {
                    field = method.invoke(v);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(String.format("%s.%s: can't get value of field", clazz.getCanonicalName(), fieldName), e);
                }
                if (field != null || !isSkipNullFields) {
                    sb.append('"')
                            .append(fieldName)
                            .append("\":");
                    myJson.getFuncToJson(field).accept(sb, field);
                    sb.append(',');
                }

            });
        }
        return func.andThen((sb, v) -> sb.setCharAt(sb.length() - 1, '}'));
    }
}
