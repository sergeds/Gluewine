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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

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
     * The classloader to use per directory.
     */
    private Map<File, ClassLoader> directories = new HashMap<File, ClassLoader>();

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

        if (root.exists())
            processDirectory(root);

        Map<File, ClassLoader> temp = new HashMap<File, ClassLoader>();
        temp.putAll(directories);
        List<File> files = new ArrayList<File>(directories.size());
        files.addAll(directories.keySet());
        Collections.sort(files, new DirectoryNameComparator());
        directories.clear();

        for (int i = 0; i < files.size(); i++)
        {
            DirectoryJarClassLoader cl = (DirectoryJarClassLoader) temp.get(files.get(i));
            directories.put(files.get(i), cl);

            // Add all previous classloaders:
            for (int j = i - 1; j >= 0; j--)
                cl.addDispatcher((DirectoryJarClassLoader) temp.get(files.get(j)));

            // Add all next classloaders:
            for (int j = i + 1; j < files.size(); j++)
                cl.addDispatcher((DirectoryJarClassLoader) temp.get(files.get(j)));
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
        jarFiles.addAll(loader.getFiles());
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File file : files)
                if (file.isDirectory()) processDirectory(file);
        }
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
     * Returns the classloader to use for the given jar.
     *
     * @param jar The jar file to use.
     * @return The classloader.
     */
    public ClassLoader getClassLoaderForJar(File jar)
    {
        if (jar.isDirectory())
            return directories.get(jar);

        else return getClassLoaderForJar(jar.getParentFile());
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
