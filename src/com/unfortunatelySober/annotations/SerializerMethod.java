package com.unfortunatelySober.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Cody on 20/12/2018.
 *
 * Denotes that this method should be used to either get or set the value within the serialized object,
 */

@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializerMethod {
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
     * Whether or not this method should be used to get or set the field, by default this information is inferred
     */
    SerializerMethodAction action() default SerializerMethodAction.INFER;
}
