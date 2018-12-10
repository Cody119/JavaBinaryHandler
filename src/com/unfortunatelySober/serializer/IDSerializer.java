package com.unfortunatelySober.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Cody on 23/09/2018.
 */
public interface IDSerializer {
    Object deserializeWith(InputStream stream, Object ... objects) throws IOException;
    void serializeWith(Object e, OutputStream stream, Object ... objects) throws IOException;

}
