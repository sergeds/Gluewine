package org.gluewine.trace;

import org.gluewine.core.AspectProvider;

/**
 * Defines a tracer.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface Tracer extends AspectProvider
{
    // ===========================================================================
    /**
     * Requests the tracer to close any open resource.
     */
    void close();
}
