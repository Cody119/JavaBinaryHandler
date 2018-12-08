package com.unfortunatelySober;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Cody on 23/09/2018.
 */
public class IntSerializer implements IISerializer {
    public static final IntSerializer INSTANCE = new IntSerializer();

    private final static int B_MASK = 0x000000FF;

    public static int readInt(InputStream stream) throws IOException {
        int i = stream.read() << 24;
        i |= stream.read() << 16;
        i |= stream.read() << 8;
        i |= stream.read();
        return i;
    }

    public static void writeInt(OutputStream stream, int i) throws IOException {
        stream.write((i >> 24) & B_MASK);
        stream.write((i >> 16) & B_MASK);
        stream.write((i >> 8) & B_MASK);
        stream.write(i & B_MASK);
    }

    protected IntSerializer() {}

    @Override
    public IDSerializer copy() {
        return this;
    }

    @Override
    public Integer deserialize(InputStream stream) throws IOException {
        return readInt(stream);
    }

    @Override
    public void serialize(Object e, OutputStream stream) throws IOException {
        writeInt(stream, ((Integer) e));
    }
}
