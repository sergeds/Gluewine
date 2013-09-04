package org.gluewine.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a method is annotated with @Rest it becomes available
 * through Rest calls.
 *
 * @author fks/Serge de Schaetzen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface REST
{
    // ===========================================================================
    /**
     * Defines the path that needs to be used to access the annotated method.
     *
     * @return The path.
     */
    String path();
}
