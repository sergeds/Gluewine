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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gluewine.launcher.GluewineClassLoader;

/**
 * A Gluewine classloader that handles a single jar/zip file.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SingleJarClassLoader extends URLClassLoader implements GluewineClassLoader
{
    // ===========================================================================
    /**
     * The jar/zip file.
     */
    private File file = null;

    /**
     * The set of package names contained in this jar file.
     */
    private Set<String> packages = new HashSet<String>();

    /**
     * The set of paths contained in this jar file.
     */
    private Set<String> paths = new HashSet<String>();

    /**
     * The classloader to dispatch unfullfilled requests to.
     */
    private GluewineClassLoader dispatcher = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param file The file to be used by this instance.
     * @param dispatcher The classloader to dispatch unfullfilled requests to.
     * @throws MalformedURLException If the file could not be mapped to a URL.
     */
    public SingleJarClassLoader(File file, GluewineClassLoader dispatcher) throws MalformedURLException
    {
        super(new URL[] {file.toURI().toURL()});
        this.dispatcher = dispatcher;
        this.file = file;
        index();
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
        return loadClass(name, true, this);
    }

    // ===========================================================================
    @Override
    public Class<?> loadClass(String name, boolean dispatch, ClassLoader invoker) throws ClassNotFoundException
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
               if (dispatch) return dispatcher.loadClass(name, true, this);
               else throw e;
            }
        }
        else
            return cl;
    }

    // ===========================================================================
    /**
     * Returns the set of package names this classloader can handle.
     */
    Set<String> getPackageNames()
    {
        Set<String> s = new HashSet<String>(packages.size());
        s.addAll(packages);
        return s;
    }

    // ===========================================================================
    /**
     * Returns the set of path names this classloader can handle.
     */
    Set<String> getPathNames()
    {
        Set<String> s = new HashSet<String>(paths.size());
        s.addAll(paths);
        return s;
    }

    // ===========================================================================
    @Override
    public URL findResource(String name)
    {
        return loadResource(name, true, this);
    }

    // ===========================================================================
    @Override
    public URL loadResource(String resource, boolean dispatch, ClassLoader invoker)
    {
        URL url = super.findResource(resource);
        if (url == null && dispatch) return dispatcher.loadResource(resource, true, this);
        else return url;
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
    /**
     * Returns the file this loader uses.
     *
     * @return The file.
     */
    public File getFile()
    {
        return file;
    }

    // ===========================================================================
    /**
     * Indexes the packages contained in this jar.
     */
    private void index()
    {
        ZipFile zf = null;
        try
        {
            zf = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory())
                {
                    String path = entry.getName();
                    if (!path.startsWith("META-INF"))
                    {
                        int i = path.lastIndexOf('/');
                        if (i > 0)
                        {
                            path = path.substring(0, i);
                            paths.add(path);
                            path = path.replace('/', '.');
                            packages.add(path);
                        }
                        else
                        {
                            packages.add("");
                            paths.add("");
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (zf != null)
            {
                try
                {
                    zf.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    // ===========================================================================
    @Override
    public String toString()
    {
        return "SJC:" + file.getAbsolutePath();
    }

    // ===========================================================================
    @Override
    public GluewineClassLoader[] getAllDispatchers()
    {
        return new GluewineClassLoader[] {dispatcher};
    }
}
