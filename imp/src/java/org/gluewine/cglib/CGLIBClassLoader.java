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
