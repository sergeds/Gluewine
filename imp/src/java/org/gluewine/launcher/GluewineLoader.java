/**************************************************************************
 *
 * Gluewine Launcher Module
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
package org.gluewine.launcher;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
     * Set of the internal classnames that have been loaded.
     */
    private Set<String> internalClassesUsed = new HashSet<String>();

    /**
     * Set of the external classnames that have been loaded.
     */
    private Set<String> externalClassesUsed = new HashSet<String>();

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
        if (loader != this && !dispatchers.contains(loader))
            dispatchers.add(loader);
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
    @Override
    public Enumeration<URL> findResources(final String name) throws IOException
    {
        Set<URL> urls = new HashSet<URL>();
        updateResources(name, urls);

        for (GluewineLoader l : dispatchers)
            l.updateResources(name, urls);

        return Collections.enumeration(urls);
    }

    // ===========================================================================
    /**
     * Updates the given set with all resources matching the given name.
     *
     * @param name The name to look for.
     * @param urls The set to update.
     * @throws IOException If an error occurs.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DMI_COLLECTION_OF_URLS") // These aren't network URLs anyway
    protected void updateResources(final String name, Set<URL> urls) throws IOException
    {
        Enumeration<URL> e = super.findResources(name);
        while (e.hasMoreElements())
        {
            urls.add(e.nextElement());
        }
    }

    // ===========================================================================
    /**
     * Tries to load the resource specified. If the resource is not found and the dispatch flag is set to true, the call is dispatched to all registered dispatchers until one can load the resource. If no one can load the requested resource, null is
     * returned.
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
                if (url != null)
                    references.add(dispatchers.get(i));
            }
        }
        return url;
    }

    // ===========================================================================
    /**
     * Returns true if this loader loaded a class or resource from the loader specified as parameter.
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
     * Tries to load the class specified. If the class is not found and the dispatch flag is set to true, the call is dispatched to all registered dispatchers until one can load the class. If no one can load the class, a ClassNotFoundException is thrown.
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
                cl = super.findClass(name);
                internalClassesUsed.add(name);
            }
            catch (ClassNotFoundException e)
            {
                if (dispatch)
                {
                    for (int i = 0; i < dispatchers.size() && cl == null; i++)
                    {
                        cl = dispatchers.get(i).loadOrDispatchClass(name, false);
                        if (cl != null)
                        {
                            if (cl.getClassLoader() instanceof GluewineLoader)
                                references.add((GluewineLoader) cl.getClassLoader());
                            externalClassesUsed.add(name);
                        }
                    }
                }
            }
        }

        if (cl == null && dispatch)
            throw new ClassNotFoundException("GluewineLoader: " + this.name + " could not load the class " + name);

        return cl;
    }

    // ===========================================================================
    /**
     * Returns the set of all classes that have been loaded internally.
     *
     * @return The set of classes.
     */
    public Set<String> getInternalClasses()
    {
        return new TreeSet<String>(internalClassesUsed);
    }

    // ===========================================================================
    /**
     * Returns the set of referenced gluewine loaders.
     *
     * @return The set of references.
     */
    public Set<String> getReferences()
    {
        Set<String> s = new TreeSet<String>();
        for (GluewineLoader l : references)
            s.add(l.getName());

        return s;
    }

    // ===========================================================================
    /**
     * Removes the given loader from the reference list.
     *
     * @param loader The loader to remove.
     */
    public void removeReference(GluewineLoader loader)
    {
        references.remove(loader);
    }

    // ===========================================================================
    /**
     * Returns the name of the classloader.
     *
     * @return The name of the classloader.
     */
    public String getName()
    {
        return name;
    }

    // ===========================================================================
    /**
     * Returns the set of all classes that have been loaded internally.
     *
     * @return The set of classes.
     */
    public Set<String> getExternalClasses()
    {
        return new TreeSet<String>(externalClassesUsed);
    }

    // ===========================================================================
    @Override
    public String toString()
    {
        return "GluewineLoader:" + name;
    }
}
