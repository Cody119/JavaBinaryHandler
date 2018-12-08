package com.unfortunatelySober;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.unfortunatelySober.old.ArrayObjectHandle;
import com.unfortunatelySober.old.ObjectSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

//TODO error handling
//TODO remove null?

class test {
    public int x;
    public int y;
    public int z;

    public test() {} //must be declared or else reflection cant find it
}

class test2 {
    public int[] x;

    public test2() {}
}

public class Main {

    public int[] x;

    static class TBuf extends ByteArrayOutputStream {
        public byte[] getBuf() {
            return buf;
        }

        public TBuf(int size) { super(size); }
    }

//    void oldtest() throws IOException {
//        ArrayObjectHandle h = new ArrayObjectHandle();
//        Arrays.asList(new Integer[]{1, 2, 3, 4, 5}).forEach(h::put);
//        TBuf b = new TBuf(1024);
//
//        new ObjectSerializer(new IDSerializer[]{
//                IntSerializer.INSTANCE,
//                IntSerializer.INSTANCE,
//                IntSerializer.INSTANCE,
//                IntSerializer.INSTANCE,
//                IntSerializer.INSTANCE
//        }).serialize(h, b);
//
////        for (int i = 0; i < b.getBuf().length; i++) {
////            System.out.print(b.getBuf()[i] + ", ");
////        }
////        System.out.println(
////);
//        ArrayObjectHandle h2 = new ArrayObjectHandle();
//        Object test = new ObjectSerializer(new IDSerializer[]{
//                IntSerializer.INSTANCE,
//                IntSerializer.INSTANCE,
//                IntSerializer.INSTANCE,
//                IntSerializer.INSTANCE,
//                IntSerializer.INSTANCE
//        }).deserialize(h2, new ByteInputStream(b.getBuf(), b.getBuf().length));
//
//        System.out.println(h2);
//    }

    public static void main(String[] args) throws Exception {
        test1();
        test2();
    }

    static void test2() throws Exception {
        CompositeSerializer x = new CompositeSerializer.Builder().Class(test.class).Names("x","z").build();
        TBuf b = new TBuf(1024);
        test t = new test();
        t.x = 17;
        t.y = 18;
        t.z = 19;

        x.serialize(t, b);
        test t2 = (test) x.deserialize(new ByteInputStream(b.getBuf(), b.getBuf().length));
        System.out.println(t2.x + ", " + t2.y + ", " + t2.z);
    }

    static void test1() throws Exception {

        Field f = test2.class.getField("x");
        Function<Object, Object> g = CompositeSerializer.getterHandle(f);
        BiConsumer<Object, Object> s = CompositeSerializer.setterHandle(f);

        CompositeSerializer y = new CompositeSerializer.Builder()
                .Constructor(test2::new)
                .Names("l", "x")
                .Getters( (o -> ((int[]) g.apply(o)).length) , (g) )
                .Setters( (o, v) -> {}, s)
                .Serializers(IntSerializer.INSTANCE, new IntArraySer())
                .Arguments(Util.s(), Util.s("l"))
                .build();

        TBuf b = new TBuf(1024);
        test2 t = new test2();
        t.x = new int[] {1, 2, 3, 4, 5, 6};


        y.serialize(t, b);
        test2 t2 = (test2) y.deserialize(new ByteInputStream(b.getBuf(), b.getBuf().length));
        return;
    }



}
