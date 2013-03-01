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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * This classloaded loads all available jar/zip files in a directory.
 * The directory is NOT processed recursively.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DirectoryJarClassLoader extends URLClassLoader
{
    // ===========================================================================
    /**
     * The set of file names that have been found.
     */
    private Set<File> loadedFiles = new HashSet<File>();

    /**
     * The 'ordered' list of classloaders requests can be dispatched to.
     */
    private List<DirectoryJarClassLoader> dispatchers = new ArrayList<DirectoryJarClassLoader>();

    /**
     * The directory managed by this loader.
     */
    private String dir = null;

    // ===========================================================================
    /**
     * Creates an instance using the specified directory.
     * All jar/zip files in the specified directory will be loaded.
     *
     * @param directory The directory to process.
     */
    public DirectoryJarClassLoader(File directory)
    {
        super(new URL[0]);
        dir = directory.getAbsolutePath();
        loadFiles(directory);
    }

    // ===========================================================================
    @Override
    public String toString()
    {
        return dir;
    }

    // ===========================================================================
    /**
     * Returns the set of files loaded by this classloader.
     *
     * @return The set of files.
     */
    public Set<File> getFiles()
    {
        Set<File> s = new TreeSet<File>();
        s.addAll(loadedFiles);
        return s;
    }

    // ===========================================================================
    /**
     * Adds a dispatcher.
     *
     * @param cl The dispatcher.
     */
    void addDispatcher(DirectoryJarClassLoader cl)
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
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        try
        {
            Class<?> cl = findLoadedClass(name);
            if (cl != null) return cl;
            else return super.findClass(name);
        }
        catch (ClassNotFoundException e)
        {
            return dispatchFindClass(name);
        }
    }

    // ===========================================================================
    /**
     * Dispatches the load request to all registered classloaders in the
     * proper order.
     *
     * @param name The class to load.
     * @return The class.
     * @throws ClassNotFoundException If no dispatcher could load the class.
     */
    private Class<?> dispatchFindClass(String name) throws ClassNotFoundException
    {
        for (DirectoryJarClassLoader cl : dispatchers)
        {
            try
            {
                return cl.findClassNoDispatch(name);
            }
            catch (ClassNotFoundException e)
            {
                // Ignore as it is bound to happen.
            }
        }

        throw new ClassNotFoundException(name);
    }

    // ===========================================================================
    @Override
    public URL findResource(String name)
    {
        URL url = super.findResource(name);
        if (url == null) return dispatchFindResource(name);
        else return url;
    }

    // ===========================================================================
    /**
     * Dispatches the load request to all registered classloaders in the
     * proper order. If no loader could load the resource null is returned.
     *
     * @param name The resource to load.
     * @return The (possibly null) URL.
     */
    private URL dispatchFindResource(String name)
    {
        URL url = null;

        for (int i = 0; i < dispatchers.size() && url == null; i++)
                url = dispatchers.get(i).findResourceNoDispatch(name);

        return url;
    }

    // ===========================================================================
    /**
     * Tries to find the resource with the given name, and returns null if
     * the resource could not be found. The call is not dispatched if the
     * resource is not found.
     *
     * @param name The resource to load.
     * @return The (possibly null) URL.
     */
    public URL findResourceNoDispatch(String name)
    {
        return super.findResource(name);
    }

    // ===========================================================================
    /**
     * Requests to load the given class without dispatching the call if the
     * class could not be found.
     *
     * @param name The name of the class to load.
     * @return The class.
     * @throws ClassNotFoundException If the class could not be found.
     */
    protected Class<?> findClassNoDispatch(String name) throws ClassNotFoundException
    {
        Class<?> cl = findLoadedClass(name);
        if (cl != null) return cl;
        else return super.findClass(name);
    }

    // ===========================================================================
    /**
     * Adds an additional file to the classloader.
     *
     * @param file The file to add.
     */
    public void addFile(File file)
    {
        try
        {
            addURL(file.toURI().toURL());
            loadedFiles.add(file);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    // ===========================================================================
    /**
     * Loads all files available in the specified directory.
     *
     * @param directory The directory to process.
     */
    private void loadFiles(File directory)
    {
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isFile())
                {
                    String name = file.getName().toLowerCase(Locale.getDefault());
                    if (name.endsWith("jar") || name.endsWith("zip"))
                        addFile(file);
                }
            }
        }
    }
}
