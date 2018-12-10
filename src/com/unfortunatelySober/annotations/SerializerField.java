package com.unfortunatelySober.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Cody on 11/12/2018.
 */

@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializerField {
    int order();
    String[] arguments() default {};
    SerializeAction action() default SerializeAction.BOTH;
}
