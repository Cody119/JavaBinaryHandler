package com.unfortunatelySober.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Cody on 23/09/2018.
 *
 * Serializer interface for independent serializers i.e. serializers that do not
 * require additional information to serialize of deserialize
 */
public interface IISerializer extends IDSerializer {
    IDSerializer copy();

    /**
     * Reads data from the input stream and returns an object created from that data
     * @param stream the input stream to use
     * @return An object created from the data in the stream
     * @throws IOException if an I/O error occurs.
     */
    Object deserialize(InputStream stream) throws IOException;

    /**
     * Serializes the object suplied into the OutputStream
     * @param e the object to serialize
     * @param stream the OutputStream to use
     * @throws IOException if an I/O error occurs.
     */
    void serialize(Object e, OutputStream stream) throws IOException;


    default Object deserializeWith(InputStream stream, Object ... objects) throws IOException {
        return deserialize(stream);
    }
    default void serializeWith(Object e, OutputStream stream, Object ... objects) throws IOException {
        serialize(e, stream);
    }
}
