package com.unfortunatelySober.serializer;

import com.unfortunatelySober.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by Cody on 10/12/2018.
 */
public class Serializers {

    private Serializers() {}

    public static final HashMap<Class, IDSerializer> SERIALIZERS = new HashMap<>();

    static {
        SERIALIZERS.put(int.class, IntSerializer.INSTANCE);
        SERIALIZERS.put(Integer.class, IntSerializer.INSTANCE);
    }

    public static IDSerializer getSerializer(Class clazz) {
        return SERIALIZERS.get(clazz);
    }

    public static BiConsumer<Object, Object> cSetterHandle(Field f) {
        Function<Object, Object> g = ReflectionUtil.getterHandle(f);
        return (o, v) -> {assert g.apply(o).equals(v);};
    }

    public static BiConsumer<Object, Object> sSetterHandle(Object oIn) {
        return (o, v) -> {assert oIn.equals(v);};
    }

    public static Function<Object, Object> sGetterHandle(Object oIn) {
        return (o) -> oIn;
    }


}
