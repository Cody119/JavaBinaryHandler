package com.unfortunatelySober.serializer;

import com.unfortunatelySober.RAList;
import com.unfortunatelySober.ReflectionUtil;
import com.unfortunatelySober.Util;
import com.unfortunatelySober.annotations.Constant;
import com.unfortunatelySober.annotations.Serializer;
import com.unfortunatelySober.annotations.SerializerField;
import com.unfortunatelySober.annotations.SerializerMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompositeSerializer implements IISerializer {

    private BiFunction<Object, Object[], Object>[] getters;
    private BiConsumer<Object, Object[]>[] setters;
    private Supplier<Object> constructor;
    private Class innerClass;
    private IDSerializer[] serializers;
    private String[] fieldNames;
    private Integer[][] argumentTags;

    private CompositeSerializer(Class clazzIn,
                               Supplier<Object> constructorIn,
                               String[] namesIn,
                               BiFunction<Object, Object[], Object>[] gettersIn,
                               BiConsumer<Object, Object[]>[] settersIn,
                               String[][] arguments,
                               IDSerializer ... serializersIn) {
        innerClass = clazzIn;
        constructor = constructorIn;
        fieldNames = namesIn;
        getters = gettersIn;
        setters = settersIn;
        serializers = serializersIn;
        argumentTags = buildArgumentTable(arguments, namesIn);
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
            Object[] arguments = Util.map(argumentTags[i], j -> objects[j], Object[]::new);
            Object oi = serializers[i].deserializeWith(stream, arguments);
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
            Object[] arguments = Util.map(argumentTags[i], j -> objects[j], Object[]::new);
            Object o = getters[i].apply(e, arguments);
            serializers[i].serializeWith(o, stream, arguments);
            objects[i] = o;
        }
    }

    public static Integer[][] buildArgumentTable(String[][] arguments, String[] names) {
        Integer[][] argumentTags;
        if (arguments != null) {
            argumentTags = new Integer[names.length][];
            HashMap<String, Integer> map = new HashMap<>(argumentTags.length);// = Util.addToMap(names, Util.rangeW(names.length));

            for (int i = 0; i < arguments.length; i++) {
                argumentTags[i] = Util.map(arguments[i], x -> {
                    Integer s = map.get(x);
                    if (s == null) throw new RuntimeException("Forward reference to " + x);
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

    public static CompositeSerializer fromClass(Class clazz) throws NoSuchMethodException {
        Serializer x;
        if ((x = (Serializer) clazz.getAnnotation(Serializer.class)) == null) {
            return new Builder().Class(clazz).build();
        } else {
            return specificBuild(x, clazz);
        }
    }

    private static CompositeSerializer specificBuild(Serializer x, Class clazz) throws NoSuchMethodException {
        Method[] methods = ReflectionUtil.getMethods(clazz, x.access());
        RAList<Method> getterMeths = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<Method> setterMeths = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<String> names = new RAList<>(Serializers.DEFAULT_SIZE);
        RAList<String[]> arguments = new RAList<>(Serializers.DEFAULT_SIZE);

        for (Method method : methods) {
            SerializerMethod serializerField;
            if ((serializerField = method.getAnnotation(SerializerMethod.class)) != null) {
                int i = serializerField.order();

                switch (serializerField.action()) {
                    case SERIALIZE_ONLY:
                        getterMeths.set(i, method);
                        break;
                    case DESERIALIZE_ONLY:
                        setterMeths.set(i, method);
                        break;
                }
                arguments.set(i, serializerField.arguments());
                names.set(i, serializerField.name().isEmpty() ? method.getName() : serializerField.name());
            }
        }

        Field[] fields = ReflectionUtil.getFields(clazz, x.access());
        RAList<Field> sortedFields = new RAList<>(Serializers.DEFAULT_SIZE);

        for (Field f : fields) {
            SerializerField serializerField;
            if ((serializerField = f.getAnnotation(SerializerField.class)) != null) {
                int i = serializerField.order();

                arguments.set(i, serializerField.arguments());
                names.set(i, f.getName());
                sortedFields.set(i, f);
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
//
//        for (int i = 0; i < size; i++) {
//            if (getterMeths.get(i) != null) {
//                getters[i] = ReflectionUtil.getterHandle(getterMeths.get(i));
//            }
//        }
//        for (int i = 0; i < size; i++) {
//            if (setterMeths.get(i) != null) {
//                setters[i] = ReflectionUtil.setterHandle(setterMeths.get(i));
//            }
//        }
//        for (int i = 0; i < sortedFields.size(); i++) {
//            if (sortedFields.get(i) != null) {
//                boolean unussed = true;
//                if (getterMeths.get(i) == null) {
//                    getters[i] = ReflectionUtil.getterHandle(sortedFields.get(i));
//                    unussed = false;
//                }
//                if (setterMeths.get(i) == null) {
//                    setters[i] = ReflectionUtil.setterHandle(sortedFields.get(i));
//                    unussed = false;
//                }
//                if (unussed) {
//                    throw new RuntimeException("Field unused: " + sortedFields.get(i).getName());
//                }
//            }
//        }

        Map<String, Class> typeMap = new HashMap<>();

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

            Class[] getterTypes = Util.map(arguments.get(i), typeMap::get, Class[]::new);
            if (gM) {
                if (!ReflectionUtil.checkArguments(getterMethod, getterTypes) || !type.isAssignableFrom(getterMethod.getReturnType()))
                    throw new RuntimeException("Argument check failed for getter");
                getters[i] = ReflectionUtil.getterHandle(getterMethod);
            } else {
                getters[i] = ReflectionUtil.getterHandle(field);
            }

            if (sM) {
                Class[] setterTypes = new Class[getterTypes.length + 1];
                System.arraycopy(getterTypes, 0, setterTypes, 1, getterTypes.length);
                setterTypes[0] = type;

                if (!ReflectionUtil.checkArguments(setterMethod, setterTypes))
                    throw new RuntimeException("Argument check failed for setter");
                setters[i] = ReflectionUtil.setterHandle(setterMethod);
            } else {
                setters[i] = ReflectionUtil.setterHandle(field);
            }


            typeMap.put(names.get(i), type);
        }

        for (int i = 0; i < getters.length; i++) {
            if (getters[i] == null || setters[i] == null) {
                throw new RuntimeException(clazz.getName() + " is missing fields for " + i);
            }
        }


        IDSerializer[] serializers = Util.map(getters, f -> IntSerializer.INSTANCE, IDSerializer[]::new);

        Supplier<Object> constructor = ReflectionUtil.constructorHandle(clazz);

        return new CompositeSerializer(clazz, constructor, names.toArray(String[]::new), getters, setters, arguments.toArray(String[][]::new), serializers);
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
        public final CompositeSerializer build() throws NoSuchMethodException {
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

            return new CompositeSerializer(clazz, constructor, names, getters, setters, arguments, serializers);
        }
    }

//        private void specificBuildt(Serializer x) {
//            Field[] fields = ReflectionUtil.getFields(clazz, x.access());
//            Field[] sorted = new Field[fields.length];
//            arguments = new String[fields.length][];
//            names = new String[fields.length];
//            for (Field f : fields) {
//                SerializerField serializerField;
//                if ((serializerField = f.getAnnotation(SerializerField.class)) != null) {
//                    int i = serializerField.order();
//                    arguments[i] = serializerField.arguments();
//                    names[i] = f.getName();
//                    sorted[i] = f;
//                }
//            }
//
//            int i = 0;
//            for (; i < sorted.length; i++) {
//                if (sorted[i] == null) {
//                    break;
//                }
//            }
//            int j = i;
//            for (; i < sorted.length; i++) {
//                if (sorted[i] != null) {
//                    i++;
//                    throw new RuntimeException(clazz.getName() + " is missing fields for " + j + " to " + i);
//                }
//            }
//            sorted = Arrays.copyOf(sorted, j, Field[].class);
//            arguments = Arrays.copyOf(arguments, j, String[][].class);
//            names = Arrays.copyOf(names, j, String[].class);
//
//            if (getters == null) {
//                GFields(sorted);
//            }
//            if (setters == null) {
//                SFields(sorted);
//            }
//            if (serializers == null) {
//                serializers = Util.map(fields, f -> Serializers.getSerializer(f.getType()), IDSerializer[]::new);
//            }
//
//        }

//    public static void test(Class clazz, Object o) {
//        try {
//            System.out.println(clazz.getField("t").get(o));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
