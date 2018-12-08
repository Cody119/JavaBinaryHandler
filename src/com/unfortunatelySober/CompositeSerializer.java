package com.unfortunatelySober;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompositeSerializer implements IISerializer {

    public static final HashMap<Class, IDSerializer> SERIALIZERS = new HashMap<>();

    static {
        SERIALIZERS.put(int.class, IntSerializer.INSTANCE);
        SERIALIZERS.put(Integer.class, IntSerializer.INSTANCE);

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

    public static IDSerializer getSerializer(Class clazz) {
        return SERIALIZERS.get(clazz);
    }

    private Function<Object, Object>[] getters;
    private BiConsumer<Object, Object>[] setters;
    private Supplier<Object> constructor;
    private Class innerClass;
    private IDSerializer[] serializers;
    private String[] fieldNames;
    private Integer[][] argumentTags;

    private CompositeSerializer(Class clazzIn,
                               Supplier<Object> constructorIn,
                               String[] namesIn,
                               Function<Object, Object>[] gettersIn,
                               BiConsumer<Object, Object>[] settersIn,
                               Integer[][] argumentTagsIn,
                               IDSerializer ... serializersIn) {
        innerClass = clazzIn;
        constructor = constructorIn;
        fieldNames = namesIn;
        getters = gettersIn;
        setters = settersIn;
        serializers = serializersIn;
        argumentTags = argumentTagsIn;
    }

    @Override
    public IDSerializer copy() {
        return null;
    }

    @Override
    public Object deserialize(InputStream stream) throws IOException {
        Object o = constructor.get();
        final Object[] objects = new Object[serializers.length];
        for (int i = 0; i < serializers.length; i++) {
            Object oi = serializers[i].deserializeWith(stream, Util.map(argumentTags[i], j -> objects[j], Object[]::new));
            setters[i].accept(o, oi);
            objects[i] = oi;
        }
        return o;
    }

    @Override
    public void serialize(Object e, OutputStream stream) throws IOException {
        for (int i = 0; i < serializers.length; i++) {
            serializers[i].serializeWith(getters[i].apply(e), stream, Util.map(argumentTags[i], j -> getters[j].apply(e), Object[]::new));
        }
    }
    //TODO lengths
    public static final class Builder {

        private Class clazz;
        private Supplier<Object> constructor;
        private String[] names;
        private Function<Object, Object>[] getters;
        private BiConsumer<Object, Object>[] setters;
        private IDSerializer[] serializers;
        private String[][] arguments;

        public Builder() {}

        public final Builder Class(Class clazzIn) {
            clazz = clazzIn;
            return this;
        }

        public final Builder Constructor(Supplier<Object> constructorIn) {
            constructor = constructorIn;
            return this;
        }

        public final Builder Names(String ... namesIn) {
            names = namesIn;
            return this;
        }

        public final Builder Serializers(IDSerializer ... serializersIn) {
            serializers = serializersIn;
            return this;
        }

        @SafeVarargs
        public final Builder Getters(Function<Object, Object> ... gettersIn) {
            getters = gettersIn;
            return this;
        }

        @SafeVarargs
        public final Builder Setters(BiConsumer<Object, Object> ... settersIn) {
            setters = settersIn;
            return this;
        }

        public final Builder GSfields(Field ... fields) {
            return GFields(fields).SFields(fields);
        }

        public final Builder GFields(Field ... fields) {

            getters = Util.map(fields, f -> getterHandle(f), Function[]::new);
            return this;
        }

        public final Builder SFields(Field ... fields) {

            setters = Util.map(fields, f -> setterHandle(f), BiConsumer[]::new);
            return this;
        }

        public final Builder Arguments(String[] ... argumentsIn) {
            arguments = argumentsIn;
            return this;
        }

        public final CompositeSerializer build() throws NoSuchMethodException {
            if (clazz == null) {
                if (Util.check(Objects::isNull, constructor, names, getters, setters, serializers)) {
                    throw new RuntimeException("Not enough information to build object");
                }
            } else {
                if (constructor == null) {
                    Constructor c = clazz.getConstructor();
                    constructor =  () -> {
                        try {
                            return c.newInstance();
                        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                            //This branch should never be reached?
                            e.printStackTrace();
                            return null;
                        }
                    };
                }
                Field[] fields;
                if (names == null) {
                    fields = clazz.getFields();
                    names = Util.map(fields, Field::getName, String[]::new);
                } else {
                    fields = getFields(clazz, names);
                }
                if (getters == null) {
                    GFields(fields);
                }
                if (setters == null) {
                    SFields(fields);
                }
                if (serializers == null) {
                    serializers = Util.map(fields, f -> getSerializer(f.getType()), IDSerializer[]::new);
                }
            }
            Integer[][] argumentTags;
            if (arguments != null) {
                argumentTags = new Integer[names.length][];
                HashMap<String, Integer> map = Util.addToMap(names, Util.rangeW(names.length));
                for (int i = 0; i < arguments.length; i++) {
                    argumentTags[i] = Util.map(arguments[i], map::get, Integer[]::new);
                }
            } else {
                argumentTags = new Integer[names.length][0];
            }
            return new CompositeSerializer(clazz, constructor, names, getters, setters, argumentTags, serializers);
        }
    }
}
