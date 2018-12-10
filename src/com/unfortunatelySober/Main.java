package com.unfortunatelySober;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.unfortunatelySober.annotations.Serializer;
import com.unfortunatelySober.annotations.SerializerField;
import com.unfortunatelySober.serializer.CompositeSerializer;
import com.unfortunatelySober.serializer.IntArraySer;
import com.unfortunatelySober.serializer.IntSerializer;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;


//TODO error handling
//TODO remove null?



public class Main {

    public int[] x;

    public static class test {
        public int x;
        public int y;
        public int z;

    }

    public static class test2 {
        public int[] x;
        public int t;

    }

    public static class test3 {
        public final int t;

        public test3() {
            t = 5;
        }

        public test3(int x) {
            t = x;
        }
    }

    @Serializer
    public static class test4 {
        @SerializerField(order = 0)
        public int x;
        @SerializerField(order = 1, arguments = {"x"})
        public int y;
        @SerializerField(order = 2, arguments = {"x", "y"})
        public int z;


    }

    static class TBuf extends ByteArrayOutputStream {
        public TBuf(byte[] x) {
            super();
            buf = x;
        }

        public byte[] getBuf() {
            return buf;
        }

        public TBuf(int size) { super(size); }
    }

    public static void main(String[] args) throws Exception {

        test1();
        test2();
        test3();
        test4();

//        CompositeSerializer.test(test3.class, new test3());
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

    static void test4() throws Exception {
        CompositeSerializer x = new CompositeSerializer.Builder().Class(test4.class).build();
        TBuf b = new TBuf(1024);
        test4 t = new test4();
        t.x = 17;
        t.y = 18;
        t.z = 19;

        x.serialize(t, b);
        test4 t2 = (test4) x.deserialize(new ByteInputStream(b.getBuf(), b.getBuf().length));
        System.out.println(t2.x + ", " + t2.y + ", " + t2.z);
    }

    static void test1() throws Exception {

        Field f = test2.class.getField("x");
        Field f1 = test2.class.getField("t");
        Function<Object, Object> g = ReflectionUtil.getterHandle(f);
        BiConsumer<Object, Object> s = ReflectionUtil.setterHandle(f);

        Function<Object, Object> g1 = ReflectionUtil.getterHandle(f1);
        BiConsumer<Object, Object> s1 = ReflectionUtil.setterHandle(f1);


        CompositeSerializer y = new CompositeSerializer.Builder()
                .Constructor(test2::new)
                .Names("l", "x", "t")
                .Getters( (o -> ((int[]) g.apply(o)).length) , (g), g1)
                .Setters( (o, v) -> {}, s, s1)
                .Serializers(IntSerializer.INSTANCE, new IntArraySer(), IntSerializer.INSTANCE)
                .Arguments(Util.s(), Util.s("l"))
                .build();

        TBuf b = new TBuf(1024);
        test2 t = new test2();
        t.x = new int[] {1, 2, 3, 4, 5, 6};
        t.t = 5;


        y.serialize(t, b);
        test2 t2 = (test2) y.deserialize(new ByteInputStream(b.getBuf(), b.getBuf().length));
        Util.with(Util.box(t2.x), x -> System.out.println(" - " + x.toString()));
        System.out.println(t.t);

        return;
    }

    public static void test3() throws Exception {
        TBuf b = new TBuf(1024);
        test t = new test();

        CompositeSerializer x = new CompositeSerializer.Builder().Class(test3.class).build();
        x.serialize(new test3(), b);
        x.serialize(new test3(10), new TBuf(b.getBuf()));

        try {
            test3 t2 = (test3) x.deserialize(new ByteInputStream(b.getBuf(), b.getBuf().length));
            System.out.println(t2.t);
        } catch (AssertionError e) {
            System.out.println("Assertion error found " + e.getMessage());
        }
    }

}
