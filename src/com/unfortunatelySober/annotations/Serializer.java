package com.unfortunatelySober.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import  java.lang.annotation.ElementType;

/**
 * Created by Cody on 9/12/2018.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Serializer {
    AccessMod access() default AccessMod.PUBLIC;
}
