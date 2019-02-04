package com.unfortunatelySober.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Cody on 23/09/2018.
 *
 * Serializer interface for dependent serializers i.e. serializers that require additional information
 * to serialize of deserialize
 */
public interface IDSerializer {
    /**
     * Reads data from the input stream and returns an object created from that data
     * @param stream the input stream to use
     * @param objects any arguments the deserializer may need (such as lengths or type infomation)
     * @return An object created from the data in the stream
     * @throws IOException if an I/O error occurs.
     */
    Object deserializeWith(InputStream stream, Object ... objects) throws IOException;

    /**
     * Serializes the object suplied into the OutputStream
     * @param e the object to serialize
     * @param stream the OutputStream to use
     * @param objects any arguments needed to serialize the object
     * @throws IOException if an I/O error occurs.
     */
    void serializeWith(Object e, OutputStream stream, Object ... objects) throws IOException;

}
