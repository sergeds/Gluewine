/**************************************************************************
 *
 * Gluewine Trace Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
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
