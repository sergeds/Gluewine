/**************************************************************************
 *
 * Gluewine DSCL Enhancer Module
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
package org.gluewine.dscl;

import java.io.InputStream;
import java.net.URL;

/**
 * Loads a class from a MemoryJavaFileManager.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DSCLClassLoader extends ClassLoader
{
    // ===========================================================================
    /**
     * The manager to use.
     */
    private DSCLJavaFileManager manager = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param manager The manager containing the newly compiled classes.
     */
    DSCLClassLoader(DSCLJavaFileManager manager)
    {
        this.manager = manager;
    }

    // ===========================================================================
    /*
     * (non-Javadoc)
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        JavaClassFromArray javaclass = manager.getJavaClass(name);
        if (javaclass == null)
            return getClass().getClassLoader().loadClass(name);

        else
        {
            byte[] b = javaclass.getClassContent();
            return defineClass(name, b, 0, b.length);
        }
    }

    // ===========================================================================
    @Override
    protected URL findResource(String name)
    {
        return getClass().getClassLoader().getResource(name);
    }

    // ===========================================================================
    @Override
    public InputStream getResourceAsStream(String name)
    {
        return getClass().getClassLoader().getResourceAsStream(name);
    }
}
