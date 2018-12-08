package com.unfortunatelySober.old;

/**
 * Created by Cody on 23/09/2018.
 */
public interface ObjectBuilder {
    ObjectBuilder put(int i, Object o);
    ObjectBuilder getBuilder(int i);
    Object build();
}
