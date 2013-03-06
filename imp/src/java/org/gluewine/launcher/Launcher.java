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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import java.util.TreeMap;

import org.gluewine.launcher.loaders.DirectoryJarClassLoader;
import org.gluewine.launcher.loaders.SingleJarClassLoader;
import org.gluewine.launcher.sources.DirectoryCodeSource;
import org.gluewine.launcher.sources.JarCodeSource;

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
     * The classloader to use per directory.
     */
    private Map<CodeSource, DirectoryJarClassLoader> directories = new HashMap<CodeSource, DirectoryJarClassLoader>();

    /**
     * The map of all available classloaders indexed on the file they loaded.
     */
    private Map<CodeSource, ClassLoader> loaders = new HashMap<CodeSource, ClassLoader>();

    /**
     * Map of parent/children CodeSources.
     */
    private Map<CodeSource, List<CodeSource>> parentChildren = new HashMap<CodeSource, List<CodeSource>>();

    /**
     * Map of children/parent CodeSources.
     */
    private Map<CodeSource, CodeSource> childrenParent = new HashMap<CodeSource, CodeSource>();

    /**
     * The map of sources indexed on their shortname
     */
    private Map<String, CodeSource> sources = new TreeMap<String, CodeSource>();

    /**
     * The list of directories, sorted using the DirectoryNameComparator.
     */
    private List<CodeSource> sortedDirectories = new ArrayList<CodeSource>();

    /**
     * The directory containing the configuration file(s).
     */
    private File configDirectory = null;

    /**
     * The file used to store the persistent map.
     */
    private File persistentFile = null;

    /**
     * The singleton instance.
     */
    private static Launcher instance = null;

    /**
     * The root directory.
     */
    private File root = null;

    /**
     * The map of persistence objects indexed on their id.
     */
    private Map<String, Serializable> persistentMap = new HashMap<String, Serializable>();

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

        String propPersist = System.getProperty("gluewine.persistfile");
        if (propPersist != null) persistentFile = new File(propPersist);
        else persistentFile = new File(configDirectory, "gluewine.state");

        if (System.getProperty("log4j.configuration") == null)
        {
            File log4j = new File(configDirectory, "log4j.properties");
            if (log4j.exists()) System.setProperty("log4j.configuration", "file:/" + log4j.getAbsolutePath().replace('\\', '/'));
        }

        loadPersistentMap();

        try
        {
            if (root.exists()) processDirectory(root);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        processMapping();
    }

    // ===========================================================================
    /**
     * Loads the persistent map from the file.
     */
    @SuppressWarnings("unchecked")
    private void loadPersistentMap()
    {
        if (persistentFile.exists())
        {
            ObjectInputStream in = null;
            try
            {
                in = new GluewineObjectInputStream(new FileInputStream(persistentFile), sources.get(getShortName(root)));
                persistentMap = (Map<String, Serializable>) in.readObject();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Returns the map of persistent properties. The map will never be null.
     */
    public Map<String, Serializable> getPersistentMap()
    {
        return persistentMap;
    }

    // ===========================================================================
    /**
     * Requests the launcher to persist the persistent map.
     */
    public void savePersistentMap()
    {
        synchronized (persistentMap)
        {
            ObjectOutputStream out = null;
            try
            {
                out = new ObjectOutputStream(new FileOutputStream(persistentFile));
                out.writeObject(persistentMap);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // ===========================================================================
    /**
     * Processes the mapping between all the DirectoryJarClassLoaders.
     */
    private void processMapping()
    {
        Map<CodeSource, DirectoryJarClassLoader> temp = new HashMap<CodeSource, DirectoryJarClassLoader>();
        temp.putAll(directories);
        Collections.sort(sortedDirectories, new CodeSourceNameComparator());
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
     * @return The CodeSource for that directory.
     * @throws IOException If an exception occurs.
     */
    private List<CodeSource> processDirectory(File dir) throws IOException
    {
        List<CodeSource> newSources = new ArrayList<CodeSource>();
        CodeSource dcs = createSourceForDirectory(dir);
        newSources.add(dcs);

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
                            CodeSource jcs = createSourceForFile(file);
                            newSources.add(jcs);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return newSources;
    }

    // ===========================================================================
    /**
     * Creates a CodeSource for the given file.
     *
     * @param file The file to process.
     * @return The code source.
     * @throws IOException If a read error occurs.
     */
    private CodeSource createSourceForFile(File file) throws IOException
    {
        String parent = getShortName(file.getParentFile());
        CodeSource pcs = sources.get(parent);
        if (pcs == null) pcs = createSourceForDirectory(file.getParentFile());

        DirectoryJarClassLoader dcl = (DirectoryJarClassLoader) pcs.getSourceClassLoader();
        SingleJarClassLoader cl = new SingleJarClassLoader(file, (GluewineClassLoader) pcs.getSourceClassLoader());
        dcl.addLoader(cl);
        JarCodeSource jcs = new JarCodeSource(file);
        jcs.setSourceClassLoader(cl);
        jcs.setDisplayName(getShortName(file));

        loaders.put(jcs, jcs.getSourceClassLoader());
        sources.put(jcs.getDisplayName(), jcs);
        parentChildren.get(pcs).add(jcs);
        childrenParent.put(jcs, pcs);

        return jcs;
    }

    // ===========================================================================
    /**
     * Creates a CodeSource for the given directory.
     *
     * @param dir The directory to process.
     * @return The code source.
     * @throws IOException If a read error occurs.
     */
    private CodeSource createSourceForDirectory(File dir) throws IOException
    {
        DirectoryJarClassLoader loader = new DirectoryJarClassLoader(dir);
        DirectoryCodeSource dcs = new DirectoryCodeSource(dir);
        dcs.setDisplayName(getShortName(dir));
        dcs.setSourceClassLoader(loader);

        sortedDirectories.add(dcs);
        directories.put(dcs, loader);
        loaders.put(dcs, dcs.getSourceClassLoader());
        sources.put(dcs.getDisplayName(), dcs);
        List<CodeSource> children = new ArrayList<CodeSource>();
        parentChildren.put(dcs, children);

        return dcs;
    }

    // ===========================================================================
    /**
     * Adds the objects specified and returns a list of codesources.
     *
     * @param toadd The objects to add.
     * @return The list of new CodeSources.
     */
    public List<CodeSource> add(List<String> toadd)
    {
        List<CodeSource> added = new ArrayList<CodeSource>();
        try
        {
            for (String s : toadd)
            {
                File file = new File(getRoot(), s);

                DirectoryJarClassLoader pcl = directories.get(file.getParentFile());
                if (pcl == null)
                    added.addAll(processDirectory(file.getParentFile()));

                else
                {
                    String name = file.getName().toLowerCase(Locale.getDefault());
                    if (name.endsWith(".jar") || name.endsWith(".zip"))
                    {
                        try
                        {
                            CodeSource source = createSourceForFile(file);
                            added.add(source);
                        }
                        catch (MalformedURLException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        processMapping();
        return added;
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
        if (file.equals(root)) return "/";
        else
        {
            String name = file.getAbsolutePath();
            name = name.substring(root.getAbsolutePath().length());
            return name.replace('\\', '/');
        }
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
     * Returns the list of available code sources.
     *
     * @return The list of codesources.
     */
    public List<CodeSource> getSources()
    {
        List<CodeSource> l = new ArrayList<CodeSource>();
        l.addAll(sources.values());
        return l;
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
     * Removes the list of objects specified.
     *
     * @param toRemove The objects to remove.
     * @return The list of removed codesources.
     */
    public List<CodeSource> remove(List<String> toRemove)
    {
        List<CodeSource> removed = new ArrayList<CodeSource>();

        for (String s : toRemove)
        {
            List<String> displayNames = new ArrayList<String>();
            displayNames.addAll(sources.keySet());
            for (String disp : displayNames)
            {
                if (disp.startsWith(s))
                {
                    CodeSource source = sources.get(disp);
                    if (source instanceof JarCodeSource)
                    {
                        CodeSource parent = childrenParent.get(source);
                        ((DirectoryJarClassLoader) parent.getSourceClassLoader()).remove((SingleJarClassLoader) source.getSourceClassLoader());
                        source.closeLoader();
                        List<CodeSource> children = parentChildren.get(parent);
                        children.remove(source);
                    }
                    else
                    {
                        DirectoryJarClassLoader dirLoader = (DirectoryJarClassLoader) source.getSourceClassLoader();
                        List<CodeSource> children = parentChildren.remove(source);
                        for (CodeSource child : children)
                        {
                            SingleJarClassLoader jarLoader = (SingleJarClassLoader) child.getSourceClassLoader();
                            dirLoader.remove(jarLoader);
                            childrenParent.remove(child);
                            loaders.remove(jarLoader);
                            sources.remove(child.getDisplayName());
                            removed.add(source);
                        }

                        sortedDirectories.remove(source);
                    }

                    loaders.remove(source);
                    sources.remove(source.getDisplayName());

                    removed.add(source);
                }
            }
        }

        // Clean up all remaining directory sources with no children:
        List<CodeSource> dirs = new ArrayList<CodeSource>(sortedDirectories.size());
        dirs.addAll(sortedDirectories);
        for (CodeSource cs : dirs)
        {
            List<CodeSource> children = parentChildren.get(cs);
            if (children != null && children.isEmpty())
            {
                sortedDirectories.remove(cs);
                loaders.remove(cs);
                sources.remove(cs.getDisplayName());
                removed.add(cs);
            }
        }

        processMapping();
        return removed;
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
            CodeSource rootCs = fw.sources.get(fw.getShortName(fw.root));
            Class<?> cl = rootCs.getSourceClassLoader().loadClass(clazz);
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
