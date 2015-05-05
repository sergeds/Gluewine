package org.gluewine.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines that the annotated method must be invoked AFTER all members have been glued.
 *
 * The method may not have any parameters.
 *
 * @author fks/Serge de Schaetzen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface RunAfterRegistration
{
    // ===========================================================================
    /**
     * Can be used to execute a method in a separate (dedicated) thread.
     */
    boolean runThreaded() default false;
}
