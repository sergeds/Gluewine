package org.gluewine.trace;

import java.util.HashSet;
import java.util.Set;


/**
 * Abstract impementation of Tracer offering some utility methods that can
 * be used by concrete implementations.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public abstract class AbstractTracer implements Tracer
{
    // ===========================================================================
    /**
     * Set of suppression flags per thread.
     */
    private Set<Thread> suppression = new HashSet<Thread>();

    // ===========================================================================
    /**
     * Returns the name of the given class. The $$EnhancerByCGLIB$$ is stripped off,
     * if present.
     *
     * @param cl The class to process.
     * @return The name of the class.
     */
    protected String getClassName(Class<?> cl)
    {
        String name = cl.getName();
        int i = name.indexOf("$$EnhancerByCGLIB$$");
        if (i > 0) name = name.substring(0, i);
        return name;
    }

    // ===========================================================================
    /**
     * Checks if tracing is suppressed for the current thread. It returns true
     * if a suppression flag is present for the current thread. If no flag is
     * present, one is created and false is returned, meaning that once this
     * method has been invoked, a clearSuppression() call must be done to clear
     * the flag.
     *
     * @return True if suppressed.
     */
    protected boolean isSuppressed()
    {
        boolean suppressed = suppression.contains(Thread.currentThread());
        if (!suppressed) suppression.add(Thread.currentThread());
        return suppressed;
    }

    // ===========================================================================
    /**
     * Clears the suppression flag of the current thread.
     */
    protected void clearSuppression()
    {
        suppression.remove(Thread.currentThread());
    }
}
