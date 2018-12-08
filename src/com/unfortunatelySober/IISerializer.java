package com.unfortunatelySober;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Cody on 23/09/2018.
 */
public interface IISerializer extends IDSerializer {
    IDSerializer copy();
    Object deserialize(InputStream stream) throws IOException;
    void serialize(Object e, OutputStream stream) throws IOException;
    default Object deserializeWith(InputStream stream, Object ... objects) throws IOException {
        return deserialize(stream);
    }
    default void serializeWith(Object e, OutputStream stream, Object ... objects) throws IOException {
        serialize(e, stream);
    }
}
