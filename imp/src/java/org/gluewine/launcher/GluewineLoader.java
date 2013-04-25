/**************************************************************************
 *
 * Gluewine Launcher Module
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
package org.gluewine.launcher;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gluewine Classloader.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineLoader extends URLClassLoader
{
    // ===========================================================================
    /**
     * The list of classloaders calls can be dispatched to.
     */
    private List<GluewineLoader> dispatchers = new ArrayList<GluewineLoader>();

    /**
     * The name of the loader. (This can be a file name, directory or a url)
     */
    private String name = null;

    /**
     * The set of references. (ie. the loaders that this loader used to resolve classes)
     */
    private Set<GluewineLoader> references = new HashSet<GluewineLoader>();

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param name The name of the classloader.
     */
    public GluewineLoader(String name)
    {
        super(new URL[0]);
        this.name = name;
    }

    // ===========================================================================
    /**
     * Adds a url.
     *
     * @param url The url to add.
     */
    public void addURL(URL url)
    {
        super.addURL(url);
    }

    // ===========================================================================
    /**
     * Adds a dispatcher.
     *
     * @param loader The loader to add.
     */
    public void addDispatcher(GluewineLoader loader)
    {
        if (loader != this && !dispatchers.contains(loader)) dispatchers.add(loader);
    }

    // ===========================================================================
    /**
     * Removes a dispatcher.
     *
     * @param loader The loader to remove.
     */
    public void removeDispatcher(GluewineLoader loader)
    {
        dispatchers.remove(loader);
    }

    // ===========================================================================
    /**
     * Clears (removes) all registered dispatchers.
     */
    public void clearDispatchers()
    {
        dispatchers.clear();
    }

    // ===========================================================================
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        return loadOrDispatchClass(name, true);
    }

    // ===========================================================================
    @Override
    public URL findResource(String name)
    {
        return loadOrDispatchResource(name, true);
    }

    // ===========================================================================
    /**
     * Tries to load the resource specified. If the resource is not found and the
     * dispatch flag is set to true, the call is dispatched to all registered
     * dispatchers until one can load the resource.
     * If no one can load the requested resource, null is returned.
     *
     * @param resource The resource to load.
     * @param dispatch True to dispatch the call.
     * @return The (possibly null) resource.
     */
    public URL loadOrDispatchResource(String resource, boolean dispatch)
    {
        URL url = super.findResource(resource);
        if (url == null && dispatch)
        {
            for (int i = 0; i < dispatchers.size() && url == null; i++)
            {
                url = dispatchers.get(i).loadOrDispatchResource(resource, false);
                if (url != null) references.add(dispatchers.get(i));
            }
        }
        return url;
    }

    // ===========================================================================
    /**
     * Returns true if this loader loaded a class or resource from the loader
     * specified as parameter.
     *
     * @param loader The loader to check.
     * @return True if referenced.
     */
    public boolean references(GluewineLoader loader)
    {
        return references.contains(loader);
    }

    // ===========================================================================
    @Override
    public void close() throws IOException
    {
        super.close();
        references.clear();
    }

    // ===========================================================================
    /**
     * Tries to load the class specified. If the class is not found and the
     * dispatch flag is set to true, the call is dispatched to all registered
     * dispatchers until one can load the class.
     * If no one can load the class, a ClassNotFoundException is thrown.
     *
     * @param name The class to load.
     * @param dispatch True to dispatch the call.
     * @return The class.
     * @throws ClassNotFoundException If the class was not found.
     */
    public Class<?> loadOrDispatchClass(String name, boolean dispatch) throws ClassNotFoundException
    {
        Class<?> cl = findLoadedClass(name);
        if (cl == null)
        {
            try
            {
                return super.findClass(name);
            }
            catch (ClassNotFoundException e)
            {
                if (dispatch)
                {
                    for (int i = 0; i < dispatchers.size() && cl == null; i++)
                    {
                        cl = dispatchers.get(i).loadOrDispatchClass(name, false);
                        if (cl != null) references.add(dispatchers.get(i));
                    }
                }
            }
        }

        if (cl == null && dispatch)
            throw new ClassNotFoundException("GluewineLoader: " + this.name + " could not load the class " + name);
        return cl;
    }

    // ===========================================================================
    @Override
    public String toString()
    {
        return "GluewineLoader:" + name;
    }
}
