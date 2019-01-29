package com.unfortunatelySober.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import  java.lang.annotation.ElementType;

/**
 * Created by Cody on 9/12/2018.
 *
 * Denotes that the class has been configured as a serializer and as such all
 * methods and fields should be ignored unless annotated correctly
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Serializer {
    /**
     * Not implemented
     */
    AccessMod access() default AccessMod.PUBLIC;

}
