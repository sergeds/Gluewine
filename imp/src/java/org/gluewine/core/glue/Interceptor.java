/**************************************************************************
 *
 * Gluewine Core Module
 *
 * Copyright (C) 2013 FKS bvba               http://www.fks.be/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; version
 * 3.0 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 **************************************************************************/
package org.gluewine.core.glue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.gluewine.core.AspectProvider;
import org.gluewine.core.InterceptChainStartOnly;

/**
 * Default interceptor. Specific enhancers (cfr. CGLIBEnhancer) should subclass this
 * class and add their specific methods to it.
 *
 * <p>The general contract for interceptors is to respect the following flow:
 * <ul>
 * <li>registerFirstInChain() - To obtain the firstInChain flag, required for some of the next calls.</li>
 * <li>invokeBefore() - This will invoke the invokeBefore method on the registered providers and update the stack of 'active' providers.</li>
 * <li>invoke the method on the object - This is implementation dependent.</li>
 * <li>invoke the afterSuccess of afterFailure depending on whether previous method call succeeded or failed.</li>
 * <li>clearThread(firstInChain) - To cleanup up</li>
 * </ul>
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class Interceptor
{
    // ===========================================================================
    /**
     * List p of registerd providers.
     */
    private List<AspectProvider> providers = new ArrayList<AspectProvider>();

    /**
     * List of providers that will only be invoked at the start of a command chain.
     */
    private List<AspectProvider> chainStartProviders = new ArrayList<AspectProvider>();

    /**
     * Set containing the thread chains.
     */
    private Set<Thread> chainThreads = new HashSet<Thread>();

    /**
     * The logger instance.
     */
    private Logger logger = Logger.getLogger(getClass());

    // ===========================================================================
    /**
     * Creates an instance.
     */
    Interceptor()
    {
    }

    // ===========================================================================
    /**
     * Registers an Aspect Provider to be used.
     *
     * @param provider The provider to register.
     */
    void register(AspectProvider provider)
    {
        logger.debug("Registering AspectProvider" + provider.getClass().getName());
        if (provider.getClass().getAnnotation(InterceptChainStartOnly.class) != null)
            chainStartProviders.add(provider);
        else
            providers.add(provider);
    }

    // ===========================================================================
    /**
     * Checks if this call is the first one in the stack of this thread.
     *
     * @return True if this method call is the first one in the chain.
     */
    public boolean registerFirstInChain()
    {
        boolean firstInChain = false;
        if (!chainThreads.contains(Thread.currentThread()))
        {
            firstInChain = true;
            chainThreads.add(Thread.currentThread());
        }
        return firstInChain;
    }

    // ===========================================================================
    /**
     * Clears the thread from the chainThreads.
     *
     * @param firstInChain True if first in chain.
     */
    public void clearThread(boolean firstInChain)
    {
        if (firstInChain)
            chainThreads.remove(Thread.currentThread());
    }

    // ===========================================================================
    /**
     * Invokes the invokeBefore method on the providers. All providers that are
     * successfully invoked will be pushed in the given stack.
     *
     * @param stack The stack to update.
     * @param o The object that is being processed.
     * @param m The method that is being executed.
     * @param params The method parameters.
     * @param firstInChain True if this method is the first in the stacktrace for the current thread.
     */
    public void invokeBefore(Stack<AspectProvider> stack, Object o, Method m, Object[] params, boolean firstInChain)
    {
        try
        {
            List<AspectProvider> tempProviders = new ArrayList<AspectProvider>();
            if (firstInChain) tempProviders.addAll(chainStartProviders);
            tempProviders.addAll(providers);

            for (AspectProvider p : tempProviders)
            {
                p.beforeInvocation(o, m, params);
                stack.push(p);
            }
        }
        catch (Throwable e)
        {
            logger.error("An error occurred invoking an beforeInvocation method: " + e.getMessage());
            invokeAfterFailure(stack, stack, m, params, e);

            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new RuntimeException(e);
        }
    }

    // ===========================================================================
    /**
     * Invokes the invokeAfterSuccess method on the providers in the given stack.
     *
     * @param stack The providers to use.
     * @param o The object that is being processed.
     * @param m The method that is being executed.
     * @param params The method parameters.
     * @param result The result of the method execution. (null if the method is void)
     */
    public void invokeAfterSuccess(Stack<AspectProvider> stack, Object o, Method m, Object[] params, Object result)
    {
        try
        {
            while (!stack.isEmpty())
            {
                AspectProvider p = stack.pop();
                p.afterSuccess(o, m, params, result);
                p.after(o, m, params);
            }
        }
        catch (RuntimeException e)
        {
            logger.error("An error occurred invoking an afterSuccess or after method: " + e.getMessage());
            invokeAfterFailure(stack, o, m, params, e);
            throw e;
        }
    }

    // ===========================================================================
    /**
     * Invokes the invokeAfterFailure method on the providers in the given stack.
     *
     * @param stack The providers to use.
     * @param o The object that is being processed.
     * @param m The method that is being executed.
     * @param params The method parameters.
     * @param e The exception thrown by the method.
     */
    public void invokeAfterFailure(Stack<AspectProvider> stack, Object o, Method m, Object[] params, Throwable e)
    {
        while (!stack.isEmpty())
        {
            try
            {
                AspectProvider p = stack.pop();
                p.afterFailure(o, m, params, e);
                p.after(o, m, params);
            }
            catch (Throwable t)
            {
                logger.error("An error occurred invoking an afterFailure or after method: " + t.getMessage());
            }
        }
    }
}
