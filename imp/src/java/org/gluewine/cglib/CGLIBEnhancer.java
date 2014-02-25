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

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.gluewine.core.ClassEnhancer;
import org.gluewine.core.glue.Interceptor;


/**
 * Default implementation of Enhancer. It uses CGLIB to enhance objects.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class CGLIBEnhancer implements ClassEnhancer
{
    // ===========================================================================
    /**
     * The interceptor to use when enhancing objects.
     */
    private CGLIBInterceptor interceptor = null;

    // ===========================================================================
    /**
     * The interceptor to use.
     *
     * @param interceptor The interceptor to use.
     */
    public CGLIBEnhancer(Interceptor interceptor)
    {
        this.interceptor = new CGLIBInterceptor(interceptor);
    }

    // ===========================================================================
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getEnhanced(final Class<T> c) throws Throwable
    {
        return (T) AccessController.doPrivileged(new PrivilegedAction<T>()
        {
            public T run()
            {
                net.sf.cglib.proxy.Enhancer e = new net.sf.cglib.proxy.Enhancer();
                e.setSuperclass(c);
                e.setClassLoader(new CGLIBClassLoader(c.getClassLoader()));
               // e.setClassLoader(c.getClassLoader());
                e.setCallback(interceptor);
                return (T) e.create();
            }
        });
    }
}
