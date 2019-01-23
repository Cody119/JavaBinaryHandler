package com.unfortunatelySober;

import java.util.Arrays;
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

    public static <T> void with(T[] ar, Consumer<T> f) {
        for (int i = 0; i < ar.length; i++) {
            f.accept(ar[i]);
        }
    }

    public static <T, R> R[] map(T[] ar, Function<T, R> f, Function<Integer, R[]> con) {
        return map(ar, f, con.apply(ar.length));
    }

    public static <T, R> R[] map(T[] ar, Function<T, R> f, Function<Integer, R[]> con, int s) {
        return map(ar, f, con.apply(s + ar.length), s);
    }

    /**
     *
     * @param ar
     * @param f
     * @param ret
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> R[] map(T[] ar, Function<T, R> f, R[] ret) {
        return map(ar, f, ret, 0);
    }

    public static <T, R> R[] map(T[] ar, Function<T, R> f, R[] ret, int s) {
        for (int i = s; i < ar.length; i++) {
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

    public static <T> T[] ensureSize(T[] original, int index, Class<? extends T[]> newType) {
        if (index+1 > original.length) {
            return Arrays.copyOf(original, index+1, newType);
        } else {
            return original;
        }
    }

    public final static class Statics{
        public static String[] s (String ...args){
            return args;
        }

        public static Integer[] box ( int[] x){
            Integer[] ret = new Integer[x.length];
            for (int i = 0; i < x.length; i++) {
                ret[i] = x[i];
            }
            return ret;
        }

        public static Character[] box ( char[] x){
            Character[] ret = new Character[x.length];
            for (int i = 0; i < x.length; i++) {
                ret[i] = x[i];
            }
            return ret;
        }

        public static Long[] box ( long[] x){
            Long[] ret = new Long[x.length];
            for (int i = 0; i < x.length; i++) {
                ret[i] = x[i];
            }
            return ret;
        }

        public static Float[] box ( float[] x){
            Float[] ret = new Float[x.length];
            for (int i = 0; i < x.length; i++) {
                ret[i] = x[i];
            }
            return ret;
        }

        public static Double[] box ( double[] x){
            Double[] ret = new Double[x.length];
            for (int i = 0; i < x.length; i++) {
                ret[i] = x[i];
            }
            return ret;
        }
    }
}
