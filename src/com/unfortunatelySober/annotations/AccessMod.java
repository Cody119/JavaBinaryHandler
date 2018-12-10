package com.unfortunatelySober.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Cody on 11/12/2018.
 */
public enum AccessMod {
    PUBLIC(Modifier.PUBLIC),
    PROTECTED(Modifier.PROTECTED),
    PRIVATE(Modifier.PRIVATE),
    PUBLIC_PRIVATE(Modifier.PRIVATE | Modifier.PUBLIC),
    PUBLIC_PROTECTED(Modifier.PROTECTED | Modifier.PUBLIC),
    PRIVATE_PROTECTED(Modifier.PRIVATE | Modifier.PROTECTED);

    private int bits;
    AccessMod(int bitsIn) {
        bits = bitsIn;
    }

    public boolean is(Field f) {
        return is(f.getModifiers());
    }

    public boolean is(int mod) {
        return (mod & bits) != 0;
    }
    //Not really needed, just to cover all bases
    public boolean is (AccessMod a) {
        return is(a.bits);
    }
}
