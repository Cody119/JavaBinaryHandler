package com.unfortunatelySober.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Cody on 11/12/2018.
 *
 * Denotes that a field should be included in the serialization and deserialization of the
 * class it belongs to
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializerField {
    /**
     * The order of the field this method is related to
     */
    int order();
    /**
     * The name of the field this method is related to
     */
    String name() default "";
    /**
     * the names of all the additional fields that should be supplied to this method, in order
     */
    String[] arguments() default {};

    /**
     * Not implemented
     */
    SerializeFieldAction action() default SerializeFieldAction.BOTH;
}
