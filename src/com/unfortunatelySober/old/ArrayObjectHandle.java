package com.unfortunatelySober.old;

import java.util.ArrayList;

/**
 * Created by Cody on 23/09/2018.
 */
public class ArrayObjectHandle implements ObjectExtractor, ObjectBuilder {

    private ArrayList<Object> oIn;

    public ArrayObjectHandle() {
        oIn = new ArrayList<>();
    }

    public ArrayObjectHandle(ArrayList<Object> oIn) {
        this.oIn = oIn;
    }

    @Override
    public ObjectBuilder put(int i, Object o) {
        oIn.ensureCapacity(i+1);
        for (int j = oIn.size(); j < i+1; j++) {
            oIn.add(null);
        }
        oIn.set(i, o);
        return this;
    }

    public ObjectBuilder put(Object o) {
        oIn.add(o);
        return this;
    }

    @Override
    public ObjectBuilder getBuilder(int i) {
        return new ArrayObjectHandle();
    }

    @Override
    public Object build() {
        return oIn.toArray();
    }

    @Override
    public ObjectExtractor get(final int i) {
        return () -> oIn.get(i);
    }

    @Override
    public Object get() {
        return oIn;
    }
}
