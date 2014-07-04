package org.gluewine.gluedgwt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates the servlet path for a GWT service.
 *
 * @author fks/Frank Gevaerts
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServicePath
{
    /**
     * The path to register the service at.
     */
    String path();
}

