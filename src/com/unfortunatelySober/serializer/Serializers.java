package com.unfortunatelySober.serializer;

import com.unfortunatelySober.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by Cody on 10/12/2018.
 */
public class Serializers {

    public static final int DEFAULT_SIZE = 10;

    private Serializers() {}

    public static final Map<Class, IDSerializer> SERIALIZERS = new HashMap<>();

    static {
        SERIALIZERS.put(int.class, IntSerializer.INSTANCE);
        SERIALIZERS.put(Integer.class, IntSerializer.INSTANCE);
        SERIALIZERS.put(int[].class, new IntArraySer());
    }

    public static IDSerializer getSerializer(Class clazz) {
        IDSerializer s = SERIALIZERS.get(clazz);
        if (s == null) {
            s = CompositeSerializer.fromClass(clazz);
            SERIALIZERS.put(clazz, s);
        }
        return s;
    }

    public static BiConsumer<Object, Object[]> cSetterHandle(Field f) {
        BiFunction<Object, Object[], Object> g = ReflectionUtil.getterHandle(f);
        return (o, v) -> {assert g.apply(o, null).equals(v);};
    }

    public static BiConsumer<Object, Object> sSetterHandle(Object oIn) {
        return (o, v) -> {assert oIn.equals(v);};
    }

    public static Function<Object, Object> sGetterHandle(Object oIn) {
        return (o) -> oIn;
    }


}
