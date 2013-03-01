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
                e.setCallback(interceptor);
                return (T) e.create();
            }
        });
    }
}
