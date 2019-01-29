package com.unfortunatelySober.serializer;

import com.unfortunatelySober.util.RAList;
import com.unfortunatelySober.util.ReflectionUtil;
import com.unfortunatelySober.util.Util;
import com.unfortunatelySober.annotations.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CompositeSerializer implements IISerializer {

    private BiFunction<Object, Object[], Object>[] getters;
    private BiConsumer<Object, Object[]>[] setters;
    private Supplier<Object> constructor;
    private Class innerClass;
    private IDSerializer[] serializers;
    private String[] fieldNames;
    private int[][] gArgumentTags;
    private int[][] sArgumentTags;
    private int[][] serArgumentTags;
    private int[][] deserArgumentTags;


    private CompositeSerializer(Class clazzIn,
                               Supplier<Object> constructorIn,
                               String[] namesIn,
                               BiFunction<Object, Object[], Object>[] gettersIn,
                               BiConsumer<Object, Object[]>[] settersIn,
                               String[][] getterArguments,
                               String[][] setterArguments,
                               String[][] serializersArguments,
                               String[][] deserializersArguments,
                               IDSerializer ... serializersIn) {
        innerClass = clazzIn;
        constructor = constructorIn;
        fieldNames = namesIn;
        getters = gettersIn;
        setters = settersIn;
        serializers = serializersIn;
        gArgumentTags = buildArgumentTable(getterArguments, namesIn);
        sArgumentTags = buildArgumentTable(setterArguments, namesIn);
        serArgumentTags = buildArgumentTable(serializersArguments, namesIn);
        deserArgumentTags = buildArgumentTable(deserializersArguments, namesIn);
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

            Object[] arguments = Util.map(deserArgumentTags[i], j -> objects[j], new Object[deserArgumentTags[i].length]);
            Object oi = serializers[i].deserializeWith(stream, arguments);

            arguments = Util.map(sArgumentTags[i], j -> objects[j], new Object[sArgumentTags[i].length]);
            Object[] arguments2 = new Object[arguments.length + 1];
            System.arraycopy(arguments, 0, arguments2, 1, arguments.length);
            arguments2[0] = oi;
            setters[i].accept(o, arguments2);
            objects[i] = oi;
        }
        return o;
    }

    @Override
    public void serialize(Object e, OutputStream stream) throws IOException {
        final Object[] objects = new Object[serializers.length];
        for (int i = 0; i < serializers.length; i++) {
            Object[] arguments = Util.map(gArgumentTags[i], j -> objects[j], new Object[gArgumentTags[i].length]);
            Object o = getters[i].apply(e, arguments);

            arguments = Util.map(serArgumentTags[i], j -> objects[j], new Object[serArgumentTags[i].length]);
            serializers[i].serializeWith(o, stream, arguments);
            objects[i] = o;
        }
    }

    public static int[][] buildArgumentTable(String[][] arguments, String[] names) {
        int[][] argumentTags;
        if (arguments != null) {
            argumentTags = new int[names.length][];
            HashMap<String, Integer> map = new HashMap<>(argumentTags.length);// = Util.addToMap(names, Util.rangeW(names.length));

            for (int i = 0; i < arguments.length; i++) {
                argumentTags[i] = arguments[i] == null ?
                    new int[0] : Util.map(arguments[i], x -> {
                        Integer s = map.get(x);
                        if (s == null)
                            throw new RuntimeException("Forward reference to " + x);
                        return s;
                });

                //Gradually add to the map so no forward reference's exist
                map.put(names[i], i);
            }
            //arguments array can be less then the name array, so fill in the rest
            for (int i = arguments.length; i < names.length; i++) {
                argumentTags[i] = new int[0];
            }
        } else {
            argumentTags = new int[names.length][0];
        }

        return argumentTags;
    }

    public static CompositeSerializer fromClass(Class clazz) {
        Serializer x;
        if ((x = (Serializer) clazz.getAnnotation(Serializer.class)) == null) {
            return new Builder().Class(clazz).build();
        } else {
            return specificBuild(x, clazz);
        }
    }

    private static CompositeSerializer specificBuild(Serializer x, Class clazz) {
        Method[] methods = ReflectionUtil.getMethods(clazz, x.access());
        RAList<Method> getterMeths = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<Method> setterMeths = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<String> names = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<String[]> getterArguments = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<String[]> setterArguments = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<String[]> fieldArguments = new RAList<>(Serializers.DEFAULT_SIZE);

        RAList<String[]> serializerArguments = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<String[]> deserializerArguments = new RAList<>(Serializers.DEFAULT_SIZE);

        Field[] fields = ReflectionUtil.getFields(clazz, x.access());
        RAList<Field> sortedFields = new RAList<>(Serializers.DEFAULT_SIZE);

        for (Method method : methods) {
            SerializerMethod serializerField;
            if ((serializerField = method.getAnnotation(SerializerMethod.class)) != null) {
                int i = serializerField.order();

                switch (serializerField.action()) {
                    case SERIALIZE_ONLY:
                        getterMeths.set(i, method);
                        getterArguments.set(i, serializerField.arguments());
                        break;
                    case DESERIALIZE_ONLY:
                        setterMeths.set(i, method);
                        setterArguments.set(i, serializerField.arguments());
                        break;
                    case INFER:
                        if (method.getReturnType() != void.class) {
                            getterMeths.set(i, method);
                            getterArguments.set(i, serializerField.arguments());
                        } else if (method.getParameterTypes().length > 0) {
                            setterMeths.set(i, method);
                            setterArguments.set(i, serializerField.arguments());
                        } else {
                            throw new RuntimeException("Could not infer method " + method.getName());
                        }
                        break;
                }

                SerializerConfig serializerConfig;
                if ((serializerConfig = method.getAnnotation(SerializerConfig.class)) != null) {
                    serializerArguments.set(i, serializerConfig.serializerArguments());
                    deserializerArguments.set(i, serializerConfig.deserializerArguments());
                }

                names.set(i, serializerField.name().isEmpty() ? method.getName() : serializerField.name());
            }
        }

        for (Field f : fields) {
            SerializerField serializerField;
            if ((serializerField = f.getAnnotation(SerializerField.class)) != null) {
                int i = serializerField.order();

                fieldArguments.set(i, serializerField.arguments());
                names.set(i, f.getName());
                sortedFields.set(i, f);

                SerializerConfig serializerConfig;
                if ((serializerConfig = f.getAnnotation(SerializerConfig.class)) != null) {
                    serializerArguments.set(i, serializerConfig.serializerArguments());
                    deserializerArguments.set(i, serializerConfig.deserializerArguments());
                }
            }
        }

        int size = Math.max(Math.max(getterMeths.size(), setterMeths.size()), sortedFields.size());
        int lastIndex = size-1;
        if (getterMeths.size() < size) {
            getterMeths.set(lastIndex, null);
        }
        if (setterMeths.size() < size) {
            setterMeths.set(lastIndex, null);
        }
        if (sortedFields.size() < size) {
            sortedFields.set(lastIndex, null);
        }


        BiFunction<Object, Object[], Object>[] getters = new BiFunction[size];
        BiConsumer<Object, Object[]>[] setters = new BiConsumer[size];


        Map<String, Class> typeMap = new HashMap<>();
        String[][] getterArgumentsNames = new String[size][];
        String[][] setterArgumentsNames = new String[size][];

        Class[] types = new Class[size];

        for (int i = 0; i < size; i++) {
            Method getterMethod = getterMeths.get(i);
            Field field = sortedFields.get(i);
            Method setterMethod = setterMeths.get(i);

            boolean gM = getterMethod != null;
            boolean sM = setterMethod != null;
            boolean fM = field != null;

            if (fM && gM && sM) throw new RuntimeException("Field unused: " + field.getName());
            if (!fM && !(gM || sM)) throw new RuntimeException("no handle for: " + i);

            Class type = ReflectionUtil.typeDiscern(
                    gM ? getterMethod.getReturnType() : null,
                    sM ? setterMethod.getParameterTypes()[0] : null,
                    fM ? field.getType() : null
            );

            if (type == null) throw new RuntimeException("Type Discern failed");



            if (gM) {
                Class[] getterTypes = Util.map(getterArguments.get(i), typeMap::get, Class[]::new);
                if (!ReflectionUtil.checkArguments(getterMethod, getterTypes) || !type.isAssignableFrom(getterMethod.getReturnType()))
                    throw new RuntimeException("Argument check failed for getter");
                getters[i] = ReflectionUtil.getterHandle(getterMethod);
                getterArgumentsNames[i] = getterArguments.get(i);
            } else {
                getters[i] = ReflectionUtil.getterHandle(field);
                getterArgumentsNames[i] = fieldArguments.get(i);
            }

            if (sM) {
                Class[] setterTypes = Util.map(setterArguments.get(i), typeMap::get, Class[]::new, 1);
                setterTypes[0] = type;

                if (!ReflectionUtil.checkArguments(setterMethod, setterTypes))
                    throw new RuntimeException("Argument check failed for setter");
                setters[i] = ReflectionUtil.setterHandle(setterMethod);
                setterArgumentsNames[i] = setterArguments.get(i);
            } else {
                setters[i] = ReflectionUtil.setterHandle(field);
                setterArgumentsNames[i] = fieldArguments.get(i);
            }

            types[i] = type;
            typeMap.put(names.get(i), type);
        }

        for (int i = 0; i < getters.length; i++) {
            if (getters[i] == null || setters[i] == null) {
                throw new RuntimeException(clazz.getName() + " is missing fields for " + i);
            }
        }


        IDSerializer[] serializers = new IDSerializer[size];
        for (int i = 0; i < size; i++) {
            serializers[i] = Serializers.getSerializer(types[i]);
        }

        Supplier<Object> constructor = ReflectionUtil.constructorHandle(clazz);

        return new CompositeSerializer(
                clazz,
                constructor,
                names.toArray(new String[names.size()]),
                getters,
                setters,
                getterArgumentsNames, setterArgumentsNames,
                serializerArguments.toArray(new String[names.size()][]),
                deserializerArguments.toArray(new String[names.size()][]),
                serializers);
    }

    //TODO lengths
    public static final class Builder {

        private Class clazz;
        private Supplier<Object> constructor;
        private String[] names;
        private BiFunction<Object, Object[],Object>[] getters;
        private BiConsumer<Object, Object[]>[] setters;
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
        public final Builder Getters(BiFunction<Object, Object[], Object> ... gettersIn) {
            getters = gettersIn;
            return this;
        }

        @SafeVarargs
        public final Builder Setters(BiConsumer<Object, Object[]> ... settersIn) {
            setters = settersIn;
            return this;
        }

        public final Builder GSfields(Field ... fields) {
            return GFields(fields).SFields(fields);
        }

        public final Builder GFields(Field ... fields) {

            getters = Util.map(fields, ReflectionUtil::getterHandle, BiFunction[]::new);
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
        public final CompositeSerializer build() {
            if (clazz == null) {
                if (Util.check(Objects::isNull, constructor, names, getters, setters, serializers)) {
                    throw new RuntimeException("Not enough information to build object");
                }
            } else {
                if (constructor == null) {
                    constructor = ReflectionUtil.constructorHandle(clazz);
                }
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

            return new CompositeSerializer(clazz, constructor, names, getters, setters, arguments, arguments, arguments, arguments, serializers);
        }
    }
}
