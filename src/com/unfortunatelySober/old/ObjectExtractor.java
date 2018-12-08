package com.unfortunatelySober.old;

/**
 * Created by Cody on 23/09/2018.
 */
public interface ObjectExtractor {
    default ObjectExtractor get(int i) {
        throw new RuntimeException("Malformed object structure");
    }
    Object get();
}
