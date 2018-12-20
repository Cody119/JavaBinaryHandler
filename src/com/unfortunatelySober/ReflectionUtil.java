package com.unfortunatelySober;

import com.unfortunatelySober.annotations.AccessMod;
import sun.reflect.Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

    public static Method[] getMethods(Class clazz, AccessMod a) {
        if (a == AccessMod.PUBLIC) {
            return clazz.getMethods();
        } else {
            throw new RuntimeException("NOT IMPLEMENTED");
        }
    }

    public static Supplier<Object> constructorHandle(Class clazz) {
        try {
            final Constructor c = clazz.getConstructor();
            return  () -> {
                try {
                    return c.newInstance();
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    //This branch should never be reached?
                    e.printStackTrace();
                    return null;
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
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

    public static Function<Object, Object> getterHandle(Method m) {
        return (o) -> {
            try {
                return m.invoke(o);
            } catch (Exception e) {
                throw new RuntimeException(e);
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

    public static BiConsumer<Object, Object> setterHandle(Method m) {
        return (o, v) -> {
            try {
                m.invoke(o, v);
            } catch (Exception e) {
                throw new RuntimeException(e);
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

    public static boolean checkArguments(Method m, Class[] arguments) {
        Class[] testArgs = m.getParameterTypes();
        if (m.isVarArgs()) {
            int normalArgLength = testArgs.length-1;
            if (normalArgLength > arguments.length) return false;
            if (!checkArguments(testArgs, arguments, normalArgLength)) return false;
            Class varType = testArgs[normalArgLength].getComponentType();
            for (int i = normalArgLength; i < arguments.length; i++) {

                if (!varType.isAssignableFrom(arguments[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return checkArguments(testArgs, arguments);
        }
    }

    public static boolean checkArguments(Class[] args1, Class[] args2) {
        if (args1.length == args2.length) return false;
        return checkArguments(args1, args2, args1.length);
    }

    public static boolean checkArguments(Class[] args1, Class[] args2, int count) {
        for (int i = 0; i < count; i++) {
            if (!args1[i].isAssignableFrom(args2[i])) {
                return false;
            }
        }
        return true;
    }
}
