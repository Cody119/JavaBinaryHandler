package com.unfortunatelySober.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Cody on 7/12/2018.
 * TODO independent serialise/deserialise only
 */
public class IntArraySer implements IDSerializer {
    @Override
    public Object deserializeWith(InputStream stream, Object... objects) throws IOException {
        int l = (Integer) objects[0];
        int[] ret = new int[l];
        for (int i = 0; i < l; i++) {
            ret[i] = IntSerializer.INSTANCE.deserialize(stream);
        }
        return ret;
    }

    @Override
    public void serializeWith(Object e, OutputStream stream, Object... objects) throws IOException {
        int[] v = ((int[]) e);
        for (int i = 0; i < v.length; i++) {
            IntSerializer.INSTANCE.serialize(v[i], stream);
        }
    }
}
