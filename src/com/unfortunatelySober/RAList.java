package com.unfortunatelySober;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

/**
 * Created by Cody on 20/12/2018.
 */
public class RAList<E> extends ArrayList<E> {

    //This is probably faster then adding the long way
    private static Field sizeSetter;

    static {
        try {
            sizeSetter = ArrayList.class.getDeclaredField("size");
            sizeSetter.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void forceArrayListSize(ArrayList<T> list, int size) {
        list.ensureCapacity(size);
        try {
            sizeSetter.set(list, size);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public RAList(int initialCapacity) {
        super(initialCapacity);
    }

    public RAList() {
        super();
    }

    public RAList(Collection<? extends E> c) {
        super(c);
    }

    @Override
    public E set(int index, E element) {
        if (index >= size()) {
            forceArrayListSize(this, index+1);
        }
        return super.set(index, element);
    }

    public E[] toArray(Function<Integer, E[]> con) {
        return toArray(con.apply(size()));
    }
}
