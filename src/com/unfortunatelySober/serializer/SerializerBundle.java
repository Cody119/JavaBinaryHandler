package com.unfortunatelySober.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cody on 10/12/2018.
 *
 * Used by a CompositeSerializer's to get the serializers needed to serialize
 * the each field
 */
public class SerializerBundle {

    public static final int DEFAULT_SIZE = 10;
    /**
     * THe default SerializerBundle used by CompositeSerializer's
     */
    public static SerializerBundle INSTANCE = new SerializerBundle();

    static {
        INSTANCE.putSerializer(int.class, IntSerializer.INSTANCE);
        INSTANCE.putSerializer(Integer.class, IntSerializer.INSTANCE);
        INSTANCE.putSerializer(int[].class, new IntArraySer());
    }

    public SerializerBundle() {}

    protected final Map<Class, IDSerializer> serializerMap = new HashMap<>();

    /**
     * Get the serializer for the class provided
     * @param clazz the class to get the serializer for
     * @return the serializer for the class
     */
    public IDSerializer getSerializer(Class clazz) {
        IDSerializer s = serializerMap.get(clazz);
        if (s == null) {
            s = CompositeSerializer.fromClass(clazz);
            serializerMap.put(clazz, s);
        }
        return s;
    }

    /**
     *
     * @param clazz
     * @param serializer
     */
    public void putSerializer(Class clazz, IDSerializer serializer) {
        serializerMap.put(clazz, serializer);
    }
}
