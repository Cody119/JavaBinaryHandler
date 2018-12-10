package com.unfortunatelySober;

import com.unfortunatelySober.annotations.AccessMod;
import sun.reflect.Reflection;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by Cody on 11/12/2018.
 */
public class ReflectionUtil {

    private ReflectionUtil() {}

    public static Field[] getFields(Class clazz, AccessMod a) {
        if (a == AccessMod.PUBLIC) {
            return clazz.getFields();
        } else {
            throw new RuntimeException("NOT IMPLEMENTED");
        }
    }

    public static Function<Object, Object> getterHandle(Field f) {
        return (x) -> {
            try {
                return f.get(x);
            } catch (IllegalAccessException err) {
                throw new RuntimeException(err);
            }
        };
    }

    public static BiConsumer<Object, Object> setterHandle(Field f) {
        return (x, y) -> {
            try {
                f.set(x, y);
            } catch (IllegalAccessException err) {
                throw new RuntimeException(err);
            }
        };
    }

    public static Field[] getFields(Class clazz, String[] names) {
        Field[] fields = clazz.getFields();
        HashMap<String, Field> map = new HashMap<>(fields.length);

        for (Field field : fields) {
            map.put(field.getName(), field);
        }

        if (!Util.check(Objects::nonNull, fields)) {
            throw new RuntimeException("Field doesn't exist");
        }

        return Util.map(names, map::get, Field[]::new);
    }
}
