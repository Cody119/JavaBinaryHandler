package com.unfortunatelySober.util;

import com.unfortunatelySober.annotations.AccessMod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Cody on 11/12/2018.
 *
 * Utility functions that make use of reflection
 */
public class ReflectionUtil {

    private ReflectionUtil() {}

    /**
     * Gets all the fields in a class with the specific access modifier
     * @param clazz the class to get the fields from
     * @param a the access mod to look for
     * @return an array of all the fields meeting the access mod requirements
     */
    public static Field[] getFields(Class clazz, AccessMod a) {
        if (a == AccessMod.PUBLIC) {
            return clazz.getFields();
        } else {
            throw new RuntimeException("NOT IMPLEMENTED");
        }
    }

    /**
     * Gets all the methods in a class with the specific access modifier
     * @param clazz the class to get the fields from
     * @param a the access mod to look for
     * @return an array of all the methods meeting the access mod requirements
     */
    public static Method[] getMethods(Class clazz, AccessMod a) {
        if (a == AccessMod.PUBLIC) {
            return clazz.getMethods();
        } else {
            throw new RuntimeException("NOT IMPLEMENTED");
        }
    }

    /**
     * Creates a {@link Supplier Supplier} object to create instances of the class specified.
     * The class passed must have a 0 argument public constructor
     * @param clazz the class to get the constructor from
     * @return a {@link Supplier Supplier} object that creates instances of the class specified
     */
    public static Supplier<Object> constructorHandle(Class clazz) {
        try {
            final Constructor c = clazz.getConstructor();
            return  () -> {
                try {
                    return c.newInstance();
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    //This branch should never be reached
                    e.printStackTrace();
                    return null;
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *
     * @param f
     * @return
     */
    public static BiFunction<Object, Object[], Object> getterHandle(Field f) {
        return (x, v) -> {
            try {
                return f.get(x);
            } catch (IllegalAccessException err) {
                throw new RuntimeException(err);
            }
        };
    }

    public static BiFunction<Object, Object[], Object> getterHandle(Method m) {
        return (o, v) -> {
            try {
                return m.invoke(o, v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static BiConsumer<Object, Object[]> setterHandle(Field f) {
        return (x, y) -> {
            try {
                f.set(x, y[0]);
            } catch (IllegalAccessException err) {
                throw new RuntimeException(err);
            }
        };
    }

    public static BiConsumer<Object, Object[]> setterHandle(Method m) {
        return (o, v) -> {
            try {
                m.invoke(o, v);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Gets all the public fields identified by the names in the "names" argument
     * @param clazz the class to get the fields from
     * @param names the names of the fields to retrieve
     * @return an array of all the fields requested, in the order they were supplied in the "names" array
     */
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

    /**
     * Check if the method supplied can accept arguments of the types specified in the "arguments" array,
     * in the order they are given
     * <P>
     * Will check for varargs and if the
     * @param m the method to test
     * @param arguments the types, in the order that they will be suplied in, to give to the method
     * @return true if the method can be called with the parameter types given, false otherwise
     */
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

    /**
     * Determines whether each type in args2 is assignable to the type
     * with the same index in args1, essentially checks
     * {@code args1[i].isAssignableFrom(args2[i])} for each element
     * @param args1
     * @param args2
     * @return true if args2's elements can be assigned to arg1's
     */
    public static boolean checkArguments(Class[] args1, Class[] args2) {
        if (args1.length != args2.length) return false;
        return checkArguments(args1, args2, args1.length);
    }

    /**
     * Determines whether all the types up to (but not including) the type at index 'count'
     * in args2 is assignable to the type with the same index in args1, essentially checks
     * {@code args1[i].isAssignableFrom(args2[i])} for each element up to "count"
     * @param args1
     * @param args2
     * @param count the index to check up to (but not including)
     * @return true if args2's elements can be assigned to arg1's
     */
    public static boolean checkArguments(Class[] args1, Class[] args2, int count) {
        for (int i = 0; i < count; i++) {
            if (!args1[i].isAssignableFrom(args2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * determines which of the types supplied is the super of all other types given.
     * nulls are allowed and will be ignored, if none of the classes suplied meet
     * that condition null is returned
     *
     * @param types the types to be checked
     * @return The super type of all the classes given, or null if that class is not in the list
     */
    public static Class typeDiscern(Class ... types) {
        if (types.length == 0) return null;
        Class type = types[0];
        for (int i = 1; i < types.length; i++) {
            if (types[i] != null) {
                if (type == null) {
                type = types[i];
                } else {
                    if (type.isAssignableFrom(types[i])) {
                    } else if (types[i].isAssignableFrom(type)) {
                        type = types[i];
                    } else {
                        return null;
                    }
                }
            }
        }
        return type;
    }

    public static BiConsumer<Object, Object[]> cSetterHandle(Field f) {
        BiFunction<Object, Object[], Object> g = getterHandle(f);
        return (o, v) -> {assert g.apply(o, null).equals(v);};
    }

    public static BiConsumer<Object, Object> sSetterHandle(Object oIn) {
        return (o, v) -> {assert oIn.equals(v);};
    }

    public static Function<Object, Object> sGetterHandle(Object oIn) {
        return (o) -> oIn;
    }
}
