/**************************************************************************
 *
 * Gluewine CGLIB Enhancer Module
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
package org.gluewine.cglib;

import java.lang.reflect.Method;
import java.util.Stack;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.gluewine.core.AspectProvider;
import org.gluewine.core.ContextInitializer;
import org.gluewine.core.glue.Interceptor;
import org.gluewine.utils.AnnotationUtility;

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
        ContextInitializer ci = AnnotationUtility.getAnnotation(ContextInitializer.class, method, obj);

        boolean firstInChain = interceptor.registerFirstInChain(ci == null);
        Stack<AspectProvider> stack = new Stack<AspectProvider>();
        interceptor.invokeBefore(stack, obj, method, args, firstInChain, ci != null);

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
