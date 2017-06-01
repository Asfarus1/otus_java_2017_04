package ru.otus_matveev_anton.myjson;

import java.util.Objects;
import java.util.Set;

@FunctionalInterface
public interface FieldFilter{
    boolean test(String fieldName, Object value, Set<Object> objectLinks);

    default FieldFilter andThen(FieldFilter after) {
        Objects.requireNonNull(after);

        return (f,v,set) -> test(f,v,set) && after.test(f,v,set);
    }
}
