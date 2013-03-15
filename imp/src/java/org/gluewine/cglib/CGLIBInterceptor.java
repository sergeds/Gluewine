/**************************************************************************
 *
 * Gluewine CGLIB Enhancer Module
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
package org.gluewine.cglib;

import java.lang.reflect.Method;
import java.util.Stack;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.gluewine.core.AspectProvider;
import org.gluewine.core.glue.Interceptor;

/**
 * The invocation handler that allows to add AOP services to the registered
 * OSGi services.
 *
 * The invocator will invoked the before, after success, after failure and after the method has been
 * invoked.
 *
 * The AspectProviders should never invoke the actual method themselves.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class CGLIBInterceptor implements MethodInterceptor
{
    // ===========================================================================
    /**
     * The interceptor to delegate to.
     */
    private Interceptor interceptor = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param interceptor The interceptor to delegate to.
     */
    CGLIBInterceptor(Interceptor interceptor)
    {
        this.interceptor = interceptor;
    }

    // ===========================================================================
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
    {
        boolean firstInChain = interceptor.registerFirstInChain();
        Stack<AspectProvider> stack = new Stack<AspectProvider>();
        interceptor.invokeBefore(stack, obj, method, args, firstInChain);

        try
        {
            Object result = null;
            result = proxy.invokeSuper(obj, args);
            interceptor.invokeAfterSuccess(stack, obj, method, args, result);
            return result;
        }
        catch (Throwable e)
        {
            interceptor.invokeAfterFailure(stack, obj, method, args, e);
            throw e;
        }
        finally
        {
            interceptor.clearThread(firstInChain);
        }
    }
}
