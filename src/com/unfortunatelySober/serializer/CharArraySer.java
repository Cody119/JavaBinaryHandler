package com.unfortunatelySober.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Cody on 7/12/2018.
 * TODO independent serialise/deserialise only
 */
public class CharArraySer implements IDSerializer {
    @Override
    public Object deserializeWith(InputStream stream, Object... objects) throws IOException {
        int l = (Integer) objects[0];
        char[] ret = new char[l];
        for (int i = 0; i < l; i++) {
            //ret[i] = IntSerializer.INSTANCE.deserialize(stream);
            ret[i] = (char)((byte)stream.read());
        }
        return ret;
    }

    @Override
    public void serializeWith(Object e, OutputStream stream, Object... objects) throws IOException {
        char[] v = ((char[]) e);
        for (int i = 0; i < v.length; i++) {
            //IntSerializer.INSTANCE.serialize(v[i], stream);
            stream.write((byte)v[i]);
        }
    }
}
