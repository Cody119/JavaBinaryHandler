package com.unfortunatelySober;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Cody on 4/12/2018.
 */
public final class Util {

    private Util() {}

    public static <T, R> R[] map(T[] ar, Function<T, R> f, Function<Integer, R[]> con) {
        return map(ar, f, con.apply(ar.length));
    }

    public static <T> void with(T[] ar, Consumer<T> f) {
        for (int i = 0; i < ar.length; i++) {
            f.accept(ar[i]);
        }
    }

    public static <T, R> R[] map(T[] ar, Function<T, R> f, R[] ret) {
        for (int i = 0; i < ar.length; i++) {
            ret[i] = f.apply(ar[i]);
        }
        return ret;
    }

    public static <T> boolean check(Predicate<T> p, T ... in) {
        for (T v : in) {
            if (!p.test(v)) {
                return false;
            }
        }
        return true;
    }

    public static <T, R, X extends Map<T, R>> X addToMap(X map, T[] keys, R[] values) {
        assert keys.length == values.length;
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    public static <T, R> HashMap<T, R> addToMap(T[] keys, R[] values) {
        return addToMap(new HashMap<>(keys.length), keys, values);
    }

    public static int[] range(int i) {
        int[] r = new int[i];
        for (int j = 0; j < i; j++) {
            r[j] = j;
        }
        return r;
    }

    public static Integer[] rangeW(int i) {
        Integer[] r = new Integer[i];
        for (int j = 0; j < i; j++) {
            r[j] = j;
        }
        return r;
    }

    public static String[] s(String ... args) {
        return args;
    }
}
