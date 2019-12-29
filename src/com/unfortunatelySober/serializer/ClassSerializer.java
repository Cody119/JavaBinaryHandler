package com.unfortunatelySober.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClassSerializer<T> implements IISerializer {

    private CompositeSerializer m_serializer;

    public ClassSerializer(Class<T> clazz, SerializerBundle sb) {
        m_serializer = CompositeSerializer.fromClass(clazz, sb);
    }

    public ClassSerializer(Class<T> clazz) {
        m_serializer = CompositeSerializer.fromClass(clazz);
    }

    // Copy constructor
    public ClassSerializer(ClassSerializer<T> cs) {
        this.m_serializer = (CompositeSerializer) cs.m_serializer.copy();
    }

    @Override
    public IDSerializer copy() {
        return new ClassSerializer<T>(this);
    }

    @Override
    public T deserialize(InputStream stream) throws IOException {
        return (T) this.m_serializer.deserialize(stream);
    }

    @Override
    public void serialize(Object e, OutputStream stream) throws IOException {
        this.m_serializer.serialize(e, stream);
    }

    public void serializeObject(T object, OutputStream stream) throws IOException {
        this.serialize(object, stream);
    }
}
