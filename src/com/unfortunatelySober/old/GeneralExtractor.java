package com.unfortunatelySober.old;

import java.lang.reflect.Field;
import java.util.function.Function;

public class GeneralExtractor<T> implements ObjectExtractor {

    private Object oi;

    private static <T> Function<T, ObjectExtractor> functionHandle(Field f) {
        Function<Object, ObjectExtractor> e = getExtractor(f);

        return (x) -> {
            try {
                return e.apply(f.get(x));
            } catch (IllegalAccessException err) {
                throw new RuntimeException(err);
            }
        };
    }

    private static Function<Object, ObjectExtractor> getExtractor(Field f) {
        return (x) -> () -> x;
    }

    public static <T> Function<T, ObjectExtractor>[] createExtractor(Class<T> clazz) {

        Field fields[] = clazz.getFields();

        Function<T, ObjectExtractor>[] extractorsFuns = new Function[fields.length];

        for (int i = 0; i < fields.length; i++) {
            extractorsFuns[i] = functionHandle(fields[i]);
        }

        return extractorsFuns;
    }

    private final Function<T, ObjectExtractor>[] extractors;

    private GeneralExtractor(Function<T, ObjectExtractor> extractorsIn[], Object o) {
        extractors = extractorsIn;
        oi = o;
    }

    @Override
    public ObjectExtractor get(int i) {
        return extractors[i].apply((T) oi);
    }

    @Override
    public T get() {
        return (T) oi;
    }
}
