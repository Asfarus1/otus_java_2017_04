package ru.otus_matveev_anton.myjson;

import java.util.Objects;
import java.util.Set;

@FunctionalInterface
public interface FunctionPart{

    void accept(StringBuilder sb, Object obj, Set<Object> objectsLinks);

    default FunctionPart andThen(FunctionPart after) {
        Objects.requireNonNull(after);

        return (sb,v,set) -> {
            accept(sb,v,set);
            after.accept(sb,v,set);
        };
    }
}
