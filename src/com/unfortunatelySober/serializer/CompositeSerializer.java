package com.unfortunatelySober.serializer;

import com.unfortunatelySober.ReflectionUtil;
import com.unfortunatelySober.Util;
import com.unfortunatelySober.annotations.Constant;
import com.unfortunatelySober.annotations.Serializer;
import com.unfortunatelySober.annotations.SerializerField;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompositeSerializer implements IISerializer {



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

            getters = Util.map(fields, f -> ReflectionUtil.getterHandle(f), Function[]::new);
            return this;
        }

        public final Builder SFields(Field ... fields) {

            setters = Util.map(fields, f -> {
                if (Modifier.isFinal(f.getModifiers())) {
                    return Serializers.cSetterHandle(f);
                } else if (f.getAnnotation(Constant.class) != null) {
                    return Serializers.cSetterHandle(f);
                } else {
                    return ReflectionUtil.setterHandle(f);
                }
            }, BiConsumer[]::new);
            return this;
        }

        public final Builder Arguments(String[] ... argumentsIn) {
            arguments = argumentsIn;
            return this;
        }

        //TODO split cases to separate builders?
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

                Serializer x;
                if ((x = (Serializer) clazz.getAnnotation(Serializer.class)) == null) {
                    generalBuild();
                } else {
                    specificBuild(x);
                }

            }

            Integer[][] argumentTags = buildArgumentTable();
            return new CompositeSerializer(clazz, constructor, names, getters, setters, argumentTags, serializers);
        }

        private void specificBuild(Serializer x) {
            Field[] fields = ReflectionUtil.getFields(clazz, x.access());
            Field[] sorted = new Field[fields.length];
            arguments = new String[fields.length][];
            names = new String[fields.length];
            for (Field f : fields) {
                SerializerField serializerField;
                if ((serializerField = f.getAnnotation(SerializerField.class)) != null) {
                    int i = serializerField.order();
                    arguments[i] = serializerField.arguments();
                    names[i] = f.getName();
                    sorted[i] = f;
                }
            }

            int i = 0;
            for (; i < sorted.length; i++) {
                if (sorted[i] == null) {
                    break;
                }
            }
            int j = i;
            for (; i < sorted.length; i++) {
                if (sorted[i] != null) {
                    i++;
                    throw new RuntimeException(clazz.getName() + " is missing fields for " + j + " to " + i);
                }
            }
            sorted = Arrays.copyOf(sorted, j, Field[].class);
            arguments = Arrays.copyOf(arguments, j, String[][].class);
            names = Arrays.copyOf(names, j, String[].class);

            if (getters == null) {
                GFields(sorted);
            }
            if (setters == null) {
                SFields(sorted);
            }
            if (serializers == null) {
                serializers = Util.map(fields, f -> Serializers.getSerializer(f.getType()), IDSerializer[]::new);
            }

        }

        private void generalBuild() throws NoSuchMethodException {


            Field[] fields;
            if (names == null) {
                fields = clazz.getFields();
                names = Util.map(fields, Field::getName, String[]::new);
            } else {
                fields = ReflectionUtil.getFields(clazz, names);
            }
            if (getters == null) {
                GFields(fields);
            }
            if (setters == null) {
                SFields(fields);
            }
            if (serializers == null) {
                serializers = Util.map(fields, f -> Serializers.getSerializer(f.getType()), IDSerializer[]::new);
            }


        }

        private Integer[][] buildArgumentTable() {
            Integer[][] argumentTags;
            if (arguments != null) {
                argumentTags = new Integer[names.length][];
                HashMap<String, Integer> map = new HashMap<>(argumentTags.length);// = Util.addToMap(names, Util.rangeW(names.length));

                for (int i = 0; i < arguments.length; i++) {
                    argumentTags[i] = Util.map(arguments[i], x -> {
                        Integer s = map.get(x);
                        if (s == null) throw new RuntimeException("Forward reference to " + x + " in " + clazz.getName());
                        return s;
                    }, Integer[]::new);

                    //Gradually add to the map so no forward reference's exist
                    map.put(names[i], i);
                }
                //argument array can be less then the name array, so fill in the rest
                for (int i = arguments.length; i < names.length; i++) {
                    argumentTags[i] = new Integer[0];
                }
            } else {
                argumentTags = new Integer[names.length][0];
            }
            return argumentTags;
        }

    }


//    public static void test(Class clazz, Object o) {
//        try {
//            System.out.println(clazz.getField("t").get(o));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
