package com.unfortunatelySober.old;

import com.unfortunatelySober.serializer.IDSerializer;
import com.unfortunatelySober.serializer.IISerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Cody on 25/09/2018.
 */
public class ObjectSerializer implements IISerializer {

    private IDSerializer[] serializers;

    public ObjectSerializer(IDSerializer[] sIn) {
        serializers = sIn;
    }

    @Override
    public IDSerializer copy() {
        return this;
    }

//    @Override
//    public Object deserialize(Object b, InputStream stream) throws IOException {
////        for (int i = 0; i < serializers.length; i++) {
////            b.put(i, serializers[i].deserialize(b.getBuilder(i), stream));
////        }
////
////        return b.build();
//        return null;
//    }

    @Override
    public Object deserialize(InputStream stream) throws IOException {
        return null;
    }

    @Override
    public void serialize(Object e, OutputStream stream) throws IOException {
//        for (int i = 0; i < serializers.length; i++) {
//            serializers[i].serialize(e.get(i), stream);
//        }

    }
}
