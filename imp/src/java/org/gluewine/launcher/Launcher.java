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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * Launches the Gluewine framework. It acceps one parameter: The name of the class
 * to launch. If ommitted, it will use the org.gluewine.core.glue.Gluer class.
 *
 * <p>Beware that this class may not import any class other than java classes, as it
 * acts as the base classloader.
 *
 * <p>By default it will locate the directory where it was started from and look for
 * a lib subdirectory from the parent directory. All jar/zip files stored there will be
 * loaded.
 * <br>The directory where the jars are stored can be overriden using the -Dgluewine.libdir property.
 *
 * <p>Configuration files should be stored in the cfg directory, located as a subdir of the
 * parent directory the class was loaded from.
 * <br>This directory can be explicitely specified using the -Dgluewine.cfgdir property.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class Launcher
{
    // ===========================================================================
    /**
     * The list of available files.
     */
    private List<File> jarFiles = new ArrayList<File>();

    /**
     * The map of files and their short name.
     */
    private Map<File, String> jarFileNames = new HashMap<File, String>();

    /**
     * The classloader to use per directory.
     */
    private Map<File, DirectoryJarClassLoader> directories = new HashMap<File, DirectoryJarClassLoader>();

    /**
     * The map of all available classloaders indexed on the file they loaded.
     */
    private Map<File, ClassLoader> loaders = new HashMap<File, ClassLoader>();

    /**
     * The list of directories, sorted using the DirectoryNameComparator.
     */
    private List<File> sortedDirectories = new ArrayList<File>();

    /**
     * The directory containing the configuration file(s).
     */
    private File configDirectory = null;

    /**
     * The singleton instance.
     */
    private static Launcher instance = null;

    /**
     * The root directory.
     */
    private File root = null;

    // ===========================================================================
    /**
     * Creates an instance.
     */
    private Launcher()
    {
        initialize();
    }

    // ===========================================================================
    /**
     * Initializes the list of available jar files.
     */
    private void initialize()
    {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File currDir = new File(path).getParentFile();
        if (path.toLowerCase(Locale.getDefault()).endsWith(".jar")) currDir = currDir.getParentFile();

        String propLib = System.getProperty("gluewine.libdir");
        if (propLib != null) root = new File(propLib);
        else root = new File(currDir, "lib");

        String propCfg = System.getProperty("gluewine.cfgdir");
        if (propCfg != null) configDirectory = new File(propCfg);
        else configDirectory = new File(currDir, "cfg");

        if (System.getProperty("log4j.configuration") == null)
        {
            File log4j = new File(configDirectory, "log4j.properties");
            if (log4j.exists()) System.setProperty("log4j.configuration", "file:/" + log4j.getAbsolutePath().replace('\\', '/'));
        }

        if (root.exists()) processDirectory(root);

        processMapping();
    }

    // ===========================================================================
    /**
     * Processes the mapping between all the DirectoryJarClassLoaders.
     */
    private void processMapping()
    {
        Map<File, DirectoryJarClassLoader> temp = new HashMap<File, DirectoryJarClassLoader>();
        temp.putAll(directories);
        Collections.sort(sortedDirectories, new DirectoryNameComparator());
        directories.clear();

        for (int i = 0; i < sortedDirectories.size(); i++)
        {
            DirectoryJarClassLoader cl = temp.get(sortedDirectories.get(i));
            cl.clearDispatchers();
            directories.put(sortedDirectories.get(i), cl);

            // Add all previous classloaders:
            for (int j = i - 1; j >= 0; j--)
                cl.addDispatcher(temp.get(sortedDirectories.get(j)));

            // Add all next classloaders:
            for (int j = i + 1; j < sortedDirectories.size(); j++)
                cl.addDispatcher(temp.get(sortedDirectories.get(j)));
        }
    }

    // ===========================================================================
    /**
     * Processes the given directory recursively and loads all jars/zips found.
     *
     * @param dir The directory to process.
     */
    private void processDirectory(File dir)
    {
        DirectoryJarClassLoader loader = new DirectoryJarClassLoader(dir);
        directories.put(dir, loader);
        sortedDirectories.add(dir);
        loaders.put(dir, loader);

        File[] files = dir.listFiles();
        for (File file : files)
        {
            if (files != null)
            {
                if (file.isDirectory())
                    processDirectory(file);

                else
                {
                    String name = file.getName().toLowerCase(Locale.getDefault());
                    if (name.endsWith(".jar") || name.endsWith(".zip"))
                    {
                        try
                        {
                            SingleJarClassLoader cl = new SingleJarClassLoader(file, loader);
                            loader.addLoader(cl);
                            jarFiles.add(file);
                            jarFileNames.put(file, getShortName(file));
                            loaders.put(file, cl);
                        }
                        catch (MalformedURLException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Adds the files specified.
     *
     * @param files The files to add.
     */
    public void add(List<File> files)
    {
        for (File file : files)
        {
            DirectoryJarClassLoader pcl = directories.get(file.getParentFile());
            if (pcl == null)
                processDirectory(file.getParentFile());

            else
            {
                String name = file.getName().toLowerCase(Locale.getDefault());
                if (name.endsWith(".jar") || name.endsWith(".zip"))
                {
                    try
                    {
                        SingleJarClassLoader cl = new SingleJarClassLoader(file, pcl);
                        pcl.addLoader(cl);
                        jarFiles.add(file);
                        jarFileNames.put(file, getShortName(file));
                        loaders.put(file, cl);
                    }
                    catch (MalformedURLException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        processMapping();
    }

    // ===========================================================================
    /**
     * Returns the short name of the given file. This is the name starting from
     * the root library.
     *
     * @param file The file to process.
     * @return The shortname.
     */
    private String getShortName(File file)
    {
        String name = file.getAbsolutePath();
        name = name.substring(root.getAbsolutePath().length() + 1);
        return name.replace('\\', '/');
    }

    // ===========================================================================
    /**
     * Returns the set of jar file names.
     *
     * @return The set of jar file names.
     */
    public Set<String> getJarFileNames()
    {
        Set<String> s = new TreeSet<String>();
        s.addAll(jarFileNames.values());
        return s;
    }

    // ===========================================================================
    /**
     * Returns the list of available jar/zip files.
     *
     * @return The list of files.
     */
    public List<File> getJarFiles()
    {
        List<File> f = new ArrayList<File>(jarFiles.size());
        f.addAll(jarFiles);
        return f;
    }

    // ===========================================================================
    /**
     * Returns the config directory.
     *
     * @return The config directory.
     */
    public File getConfigDirectory()
    {
        return configDirectory;
    }

    // ===========================================================================
    /**
     * Returns the properties stored in the container with the given name.
     *
     * @param name The name of the property file to obtain. (unqualified).
     * @return The properties.
     * @throws IOException Thrown if the container does not exists or could not be read.
     */
    public Properties getProperties(String name) throws IOException
    {
        InputStream input = null;
        Properties props = new Properties();

        try
        {
            input = new FileInputStream(new File(configDirectory, name));
            props.load(input);
        }
        finally
        {
            if (input != null) input.close();
        }

        return props;
    }

    // ===========================================================================
    /**
     * Returns the root directory.
     *
     * @return The root directory.
     */
    public File getRoot()
    {
        return root;
    }

    // ===========================================================================
    /**
     * Returns the classloader to use for the given jar.
     *
     * @param jar The jar file to use.
     * @return The classloader.
     */
    public ClassLoader getClassLoaderForJar(File jar)
    {
        return loaders.get(jar);
    }

    // ===========================================================================
    /**
     * Removes the list of files specified.
     *
     * @param files The list of files to remove.
     */
    public void remove(List<File> files)
    {
        for (File file : files)
        {
            DirectoryJarClassLoader cl = null;
            if (file.isFile())
            {
                cl = directories.get(file.getParentFile());
                if (cl != null)
                {
                    ClassLoader loader = loaders.remove(file);
                    if (loader instanceof SingleJarClassLoader)
                    {
                        remove((SingleJarClassLoader) loader);
                        cl.remove((SingleJarClassLoader) loader);
                    }
                }
            }
            else
            {
                cl = directories.get(file);
                if (cl != null)
                {
                    for (SingleJarClassLoader sl : cl.getFileLoaders())
                    {
                        remove(sl);
                        cl.remove(sl);
                    }
                }
            }

            if (cl != null && cl.isEmpty())
            {
                this.directories.remove(cl.getDirectory());
                this.jarFileNames.remove(cl.getDirectory());
                this.jarFiles.remove(cl.getDirectory());
                this.loaders.remove(cl.getDirectory());
            }
        }

        processMapping();
    }

    // ===========================================================================
    /**
     * Removes (and closes) the given instance of the classloader.
     *
     * @param loader The loader to close.
     */
    private void remove(SingleJarClassLoader loader)
    {
        try
        {
            loader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        jarFileNames.remove(loader.getFile());
        jarFiles.remove(loader.getFile());
    }

    // ===========================================================================
    /**
     * Returns the singleton instance.
     *
     * @return The instance.
     */
    public static synchronized Launcher getInstance()
    {
        return AccessController.doPrivileged(new PrivilegedAction<Launcher>()
        {
            @Override
            public Launcher run()
            {
                if (instance == null) instance = new Launcher();
                return instance;
            }
        });
    }

    // ===========================================================================
    /**
     * Main invocation routine.
     *
     * @param args The Command line arguments.
     */
    public static void main(String[] args)
    {
        try
        {
            if (args == null || args.length < 1)
                args = new String[] {"org.gluewine.core.glue.Gluer"};

            String clazz = args[0];

            if (clazz.equals("console")) clazz = "org.gluewine.console.impl.ConsoleClient";

            Launcher fw = getInstance();
            Class<?> cl = fw.directories.get(fw.root).loadClass(clazz);
            String[] params = new String[args.length - 1];
            if (args.length > 1) System.arraycopy(args, 1, params, 0, params.length);

            cl.getMethod("main", String[].class).invoke(null, new Object[] {params});
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
