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
package org.gluewine.launcher.loaders;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gluewine.launcher.GluewineClassLoader;

/**
 * This classloader does not really load classes. It dispatch the requests
 * to the embedded instances of SingleJarClassLoader.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DirectoryJarClassLoader extends ClassLoader implements GluewineClassLoader
{
    // ===========================================================================
    /**
     * The list of actual classloaders.
     */
    private List<SingleJarClassLoader> realLoaders = new ArrayList<SingleJarClassLoader>();

    /**
     * The 'ordered' list of classloaders requests can be dispatched to.
     */
    private List<GluewineClassLoader> dispatchers = new ArrayList<GluewineClassLoader>();

    /**
     * The set of package names this loader can handle.
     */
    private Set<String> packages = new HashSet<String>();

    /**
     * The set of paths this loader can handle.
     */
    private Set<String> paths = new HashSet<String>();

    /**
     * The directory processed by this loader.
     */
    private File directory = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param dir The directory to use.
     */
    public DirectoryJarClassLoader(File dir)
    {
        this.directory = dir;
    }

    // ===========================================================================
    /**
     * Returns the directory processed.
     *
     * @return The directory.
     */
    public File getDirectory()
    {
        return directory;
    }

    // ===========================================================================
    /**
     * Clears all dispatchers.
     */
    public void clearDispatchers()
    {
        dispatchers.clear();
    }

    // ===========================================================================
    /**
     * Adds a dispatcher.
     *
     * @param cl The dispatcher.
     */
    public void addDispatcher(DirectoryJarClassLoader cl)
    {
        dispatchers.add(cl);
    }

    // ===========================================================================
    /**
     * Loads the class with the given name, by dispatching the request to all
     * available classloaders in the order they are stored. If no classloader
     * was able to load the class a {@link ClassNotFoundException} is thrown.
     *
     * @param name The name of the class to load.
     * @return The class.
     * @throws ClassNotFoundException If the class is not found.
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        return loadClass(name, true, this);
    }

    // ===========================================================================
    @Override
    public Class<?> loadClass(String name, boolean dispatch, ClassLoader invoker) throws ClassNotFoundException
    {
        Class<?> cl = null;
        int i = name.lastIndexOf('.');
        String pack = "";
        if (i > 0) pack = name.substring(0, i);

        for (i = 0; i < realLoaders.size() && cl == null; i++)
        {
            try
            {
                SingleJarClassLoader loader = realLoaders.get(i);
                if (loader != invoker && loader.canProvidePackage(pack))
                    cl = loader.loadClass(name, false, this);
            }
            catch (ClassNotFoundException e)
            {
                // Allowed to fail.
            }
        }

        if (cl != null) return cl;
        else if (dispatch)
        {
            for (i = 0; i < dispatchers.size() && cl == null; i++)
            {
                GluewineClassLoader loader = dispatchers.get(i);
                if (loader != invoker && loader.canProvidePackage(pack))
                {
                    cl = loader.loadClass(name, false, this);
                }
            }

            if (cl != null) return cl;
        }

        throw new ClassNotFoundException(toString() + " could not load " + name);
    }

    // ===========================================================================
    @Override
    public URL findResource(String name)
    {
        return loadResource(name, true, this);
    }

    // ===========================================================================
    @Override
    public URL loadResource(String name, boolean dispatch, ClassLoader invoker)
    {
        URL url = null;
        int i = name.lastIndexOf('/');
        String path = "";
        if (i > 0) path = name.substring(0, i);

        for (i = 0; i < realLoaders.size() && url == null; i++)
        {
            SingleJarClassLoader loader = realLoaders.get(i);
            if (loader != invoker && loader.canProvidePath(path))
                url = loader.loadResource(name, false, this);
        }

        if (url == null && dispatch)
        {
            for (i = 0; i < dispatchers.size() && url == null; i++)
            {
                GluewineClassLoader loader = dispatchers.get(i);
                if (loader != invoker && loader.canProvidePath(path))
                    url = loader.loadResource(name, false, this);
            }
        }

        return url;
    }

    // ===========================================================================
    /**
     * Adds an additional file to the classloader.
     *
     * @param loader The loader.
     */
    public void addLoader(SingleJarClassLoader loader)
    {
        packages.addAll(loader.getPackageNames());
        paths.addAll(loader.getPathNames());
        realLoaders.add(loader);
    }

    // ===========================================================================
    /**
     * Returns the list of class loaders.
     *
     * @return The list of loaders.
     */
    public List<SingleJarClassLoader> getFileLoaders()
    {
        List<SingleJarClassLoader> l = new ArrayList<SingleJarClassLoader>(realLoaders.size());
        l.addAll(realLoaders);
        return l;
    }

    // ===========================================================================
    /**
     * Returns true if this loader does not contain any file loader anymore.
     *
     * @return True if empty.
     */
    public boolean isEmpty()
    {
        return realLoaders.isEmpty();
    }

    // ===========================================================================
    /**
     * Removes the given loader.
     *
     * @param loader The loader to remove.
     */
    public void remove(SingleJarClassLoader loader)
    {
        realLoaders.remove(loader);
    }

    // ===========================================================================
    @Override
    public boolean canProvidePackage(String packName)
    {
        return packages.contains(packName);
    }

    // ===========================================================================
    @Override
    public boolean canProvidePath(String path)
    {
        return paths.contains(path);
    }

    // ===========================================================================
    @Override
    public String toString()
    {
        return "DJC:" + directory.getAbsolutePath();
    }

    // ===========================================================================
    @Override
    public GluewineClassLoader[] getAllDispatchers()
    {
        List<GluewineClassLoader> l = new ArrayList<GluewineClassLoader>();
        l.addAll(realLoaders);
        l.addAll(dispatchers);
        return l.toArray(new GluewineClassLoader[l.size()]);
    }
}
