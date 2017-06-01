package ru.otus_matveev_anton.myjson;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Created by asfarus on 31.05.2017.
 */
@FunctionalInterface
public interface FunctionPart extends BiConsumer<StringBuilder, Object> {
    @Override
    default FunctionPart andThen(BiConsumer<? super StringBuilder, ? super Object> after) {
        Objects.requireNonNull(after);

        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }

    default FunctionPart andBefore(BiConsumer<? super StringBuilder, ? super Object> before) {
        Objects.requireNonNull(before);

        return (l, r) -> {
            before.accept(l, r);
            accept(l, r);
        };
    }
}
