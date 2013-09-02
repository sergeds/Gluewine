package org.gluewine.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used for the parameters of an REST annotated method,
 * allowing to specify an ID to those parameters, that then must match
 * the name of GET parameters or a POST object.
 *
 * @author fks/Serge de Schaetzen
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RESTID
{
    // ===========================================================================
    /**
     * Returns the id of the parameter.
     *
     * @return The id.
     */
    String id();
}
