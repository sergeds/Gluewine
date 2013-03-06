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

import java.net.URL;

/**
 * This classloader will dispatch all requests to load classes starting with 'net.sf.cglib'
 * to the parent classloader, and all other classes to the wrapped classloader.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class CGLIBClassLoader extends ClassLoader
{
    // ===========================================================================
    /**
     * The classloader to dispatch requests to.
     */
    private ClassLoader dispatcher = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param dispatcher The loader to dispatch requests to.
     */
    CGLIBClassLoader(ClassLoader dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    // ===========================================================================
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        Class<?> c = findLoadedClass(name);

        if (c == null)
        {
            if (name.startsWith("net.sf.cglib"))
                return Class.forName(name);

            else
                return dispatcher.loadClass(name);
        }
        else return c;
    }

    // ===========================================================================
    @Override
    protected URL findResource(String name)
    {
        return dispatcher.getResource(name);
    }
}
