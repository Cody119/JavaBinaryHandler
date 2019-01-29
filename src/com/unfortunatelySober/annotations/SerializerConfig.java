package com.unfortunatelySober.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Cody on 29/01/2019.
 *
 * Annotation used to modify what serializer should be used to serialize the specific field.
 * Also needed to specify any arguments the field serializer may need to serialize the data.
 */

@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializerConfig {
    /**
     * Currently not implemented
     * The serializer that should be used, if set to void.class
     * the serializer to use will be inferred
     */
    Class type() default void.class;
    /**
     * The names of all the arguments that should be given to the serializer
     */
    String[] serializerArguments() default {};
    /**
     * The names of all the arguments that should be given to the deserializer
     */
    String[] deserializerArguments() default {};
}
