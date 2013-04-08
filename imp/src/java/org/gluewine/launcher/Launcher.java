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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gluewine.launcher.sources.DirectoryCodeSource;
import org.gluewine.launcher.sources.JarCodeSource;
import org.gluewine.launcher.sources.URLCodeSource;
import org.gluewine.launcher.utils.FileUtils;


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
public final class Launcher implements Runnable, DirectoryAnnotations
{
    // ===========================================================================
    /**
     * The codesouces indexed on the directory.
     */
    private Map<String, List<CodeSource>> codeSources = new HashMap<String, List<CodeSource>>();

    /**
     * The map of sources indexed on their shortname.
     */
    private Map<String, CodeSource> sourcesMap = new TreeMap<String, CodeSource>();

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
    private HashMap<String, Serializable> persistentMap = new HashMap<String, Serializable>();

    /**
     * The set of property file names that have been requested.
     */
    private Set<String> propertiesUsed = new HashSet<String>();

    /**
     * The set of registered listeners.
     */
    private Set<CodeSourceListener> listeners = new HashSet<CodeSourceListener>();

    /**
     * The set of directories that have a single classloader.
     */
    private Set<File> singleLoaderDirectories = new HashSet<File>();

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

        try
        {
            if (System.getProperty("log4j.configuration") == null)
            {
                File log4j = new File(configDirectory, "log4j.properties");
                if (log4j.exists()) System.setProperty("log4j.configuration", log4j.toURI().toURL().toExternalForm());
            }

            loadPersistentMap();

            if (root.exists())
            {
                List<CodeSource> srcs = loadDirectories(root);
                for (CodeSource src : srcs)
                    sourcesMap.put(src.getDisplayName(), src);

                mapLoaders();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
                in = new GluewineObjectInputStream(new FileInputStream(persistentFile), sourcesMap.get(getShortName(root)));
                persistentMap = (HashMap<String, Serializable>) in.readObject();
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
     * <br>Remark: the map returned is the actual map, not a copy!
     *
     * @return The persistent map.
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

    /**
     * Maps all classloaders.
     */
    private void mapLoaders()
    {
        List<String> sorted = new ArrayList<String>(codeSources.size());
        sorted.addAll(codeSources.keySet());
        Collections.sort(sorted);

        // Clear all existing mappings:
        for (List<CodeSource> list : codeSources.values())
            for (CodeSource cs : list)
                cs.getSourceClassLoader().clearDispatchers();

        // Remap:
        for (int i = 0; i < sorted.size(); i++)
        {
            List<CodeSource> list = codeSources.get(sorted.get(i));

            // First all CodeSources in the same directory.
            for (int j = 0; j < list.size(); j++)
                for (int k = 0; k < list.size(); k++)
                list.get(j).getSourceClassLoader().addDispatcher(list.get(k).getSourceClassLoader());

            // Then all directories lower.
            for (int j = i - 1; j >= 0; j--)
            {
                List<CodeSource> list2 = codeSources.get(sorted.get(j));
                for (CodeSource g : list)
                    for (CodeSource g2 : list2)
                        g.getSourceClassLoader().addDispatcher(g2.getSourceClassLoader());
            }

            // Finally the subdirectories.
            for (int j = i + 1; j < sorted.size(); j++)
            {
                List<CodeSource> list2 = codeSources.get(sorted.get(j));
                for (CodeSource g : list)
                    for (CodeSource g2 : list2)
                        g.getSourceClassLoader().addDispatcher(g2.getSourceClassLoader());
            }
        }
    }

    // ===========================================================================
    /**
     * Loads the given directory and all its subdirectories.
     *
     * @param dir The directory to load.
     * @return The code sources.
     * @throws IOException If a problem occurs reading the directory.
     */
    private List<CodeSource> loadDirectories(File dir) throws IOException
    {
        List<CodeSource> sources = loadDirectory(dir);
        codeSources.put(dir.getAbsolutePath(), sources);

        File[] dirs = dir.listFiles();
        if (dirs != null)
        {
            for (File file : dirs)
                if (file.isDirectory())
                    sources.addAll(loadDirectories(file));
        }

        return sources;
    }

    // ===========================================================================
    /**
     * Checks for the presence of the @Install annotation in the given
     * directory, and if present processes it.
     *
     * @param dir The directory to process.
     * @throws IOException If an error occurs reading the annotation file, or fetching
     * the resources.
     */
    private void checkInstallAnnotation(File dir) throws IOException
    {
        File annot = new File(dir, INSTALL);
        if (annot.exists())
        {
            List<String> content = FileUtils.readFile(annot);
            for (String url : content)
                fetch(new URL(url), dir);
        }

        // Delete the annotation as everything was fetched successfully.
        annot.delete();
    }

    // ===========================================================================
    /**
     * Activates the given file. The activation consists of renaming the
     * file by removing the '.notactivated' part.
     *
     * @param file The file to activate.
     * @throws IOException If an error occurs renaming the file.
     * @return The activated file.
     */
    public File activate(File file) throws IOException
    {
        String name = file.getAbsolutePath();
        int i = name.indexOf(".notactivated");
        if (i > -1)
        {
            name = name.substring(0, i);
            File target = new File(name);
            if (target.exists())
                if (!target.delete()) throw new IOException("The file " + target.getAbsolutePath() + " could not be deleted.");

            if (!file.renameTo(target)) throw new IOException("Could no activate the file " + file.getAbsolutePath());

            return target;
        }
        else
            throw new IOException("The file " + file.getAbsolutePath() + " cannot be activated.");
    }

    // ===========================================================================
    /**
     * Fetches the url given and stores it in the given directory. The file
     * receives the file name of the url, with '.notactivated' appended at
     * the end.
     *
     * @param url The url to fetch from.
     * @param dir The directory to save it into.
     * @throws IOException If an error occurs.
     */
    public File fetch(URL url, File dir) throws IOException
    {
        File target = new File(dir, url.getFile() + ".notactivated");
        if (target.exists())
            if (!target.delete()) throw new IOException("Could not delete " + target.getAbsolutePath());

        FileOutputStream out = null;
        InputStream in = null;
        try
        {
            in = url.openStream();
            out = new FileOutputStream(target);
            byte[] b = new byte[65536];
            int read = 0;
            read = in.read(b);
            while (read > -1)
            {
                out.write(b, 0, read);
                read = in.read(b);
            }
            return target;
        }
        finally
        {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }

    // ===========================================================================
    /**
     * Loads all jar/zip files located in the given directory.
     *
     * @param dir The directory to load.
     * @return The CodeSources for the given directory.
     * @throws IOException If an error occurs reading the directory.
     */
    private List<CodeSource> loadDirectory(File dir) throws IOException
    {
        List<CodeSource> sources = new ArrayList<CodeSource>();

        File annotation = new File(dir, SINGLELOADER);
        boolean single = annotation.exists();
        if (single) singleLoaderDirectories.add(dir);
        checkInstallAnnotation(dir);

        List<File> jars = new ArrayList<File>();
        // Index the content.
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                String name = file.getName().toLowerCase(Locale.getDefault());
                if (name.endsWith(".jar") || name.endsWith(".zip"))
                    jars.add(file);
            }
        }

        DirectoryCodeSource dcs = new DirectoryCodeSource(dir);
        dcs.setDisplayName(getShortName(dir));
        GluewineLoader dirLoader = new GluewineLoader(getShortName(dir));
        dcs.setSourceClassLoader(dirLoader);
        sourcesMap.put(dcs.getDisplayName(), dcs);
        sources.add(dcs);

        URL[] annotatedURLs = loadUrls(dir);

        // Create the classloader(s) for the files.
        if (single)
        {
            // One loader gets all the files.
            GluewineLoader loader = new GluewineLoader(getShortName(dir));
            for (File file : jars)
                sources.add(getCodeSource(file, loader));

            for (URL url : annotatedURLs)
                sources.add(getCodeSource(url, loader));
        }
        else
        {
            // One loader per file:
            for (File file : jars)
                sources.add(getCodeSource(file, null));

            for (URL url : annotatedURLs)
                sources.add(getCodeSource(url, null));
        }

        return sources;
    }

    // ===========================================================================
    /**
     * Creates and returns a CodeSource for the given file. If the loader
     * is null a new GluewineLoader is created. If it is not null it is
     * assigned to the CodeSource.
     *
     * @param file The file to process.
     * @param loader The (possibly null) classloader to use.
     * @return The CodeSource.
     * @throws IOException Thrown if an error occurs.
     */
    private CodeSource getCodeSource(File file, GluewineLoader loader) throws IOException
    {
        String name = getShortName(file);
        if (loader == null) loader = new GluewineLoader(name);
        loader.addURL(file.toURI().toURL());
        CodeSource src = new JarCodeSource(file);
        src.setSourceClassLoader(loader);
        src.setDisplayName(name);
        return src;
    }

    // ===========================================================================
    /**
     * Creates and returns a CodeSource for the given url. If the loader
     * is null a new GluewineLoader is created. If it is not null it is
     * assigned to the CodeSource.
     *
     * @param url The url.
     * @param loader The (possibly null) classloader to use.
     * @return The CodeSource.
     * @throws IOException Thrown if an error occurs.
     */
    private CodeSource getCodeSource(URL url, GluewineLoader loader) throws IOException
    {
        String name = url.toExternalForm();
        if (loader == null) loader = new GluewineLoader(name);
        loader.addURL(url);
        CodeSource src = new URLCodeSource(url);
        src.setSourceClassLoader(loader);
        src.setDisplayName(name);
        return src;
    }

    // ===========================================================================
    /**
     * Reads the URL annotation file (if present) and returns an array of all
     * urls defined in that file.
     *
     * @param dir The directory to process.
     * @return The array of defined urls.
     * @throws IOException If an error occurs reading the file. (or an invalud URL
     * has been defined).
     */
    @SuppressWarnings("unchecked")
    private URL[] loadUrls(File dir) throws IOException
    {
        List<URL> urls = new ArrayList<URL>();

        File annot = new File(dir, URLLOADER);
        if (annot.exists())
        {
            HashSet<String> removedUrls = (HashSet<String>) persistentMap.get("GLUEWINE:REMOVEDURLS");
            if (removedUrls == null) removedUrls = new HashSet<String>();
            List<String> ss = FileUtils.readFile(annot);
            for (String s : ss)
                if (!removedUrls.contains(s))
                    urls.add(new URL(s));
        }

        return urls.toArray(new URL[urls.size()]);
    }

    // ===========================================================================
    /**
     * Returns the URL that is used to perform updates.
     *
     * @return The URL.
     * @throws IOException If an error occurs.
     */
    public String getSourceRepositoryURL() throws IOException
    {
        String repos = (String) persistentMap.get("GLUEWINE::REPOSITORY");
        if (repos == null)
            repos = root.toURI().toURL().toExternalForm();

        if (!repos.endsWith("/")) repos = repos + "/";

        return repos;
    }

    // ===========================================================================
    /**
     * Sets the url to use for updates.
     *
     * @param url The url.
     */
    public void setSourceRepositoryURL(String url)
    {
        persistentMap.put("GLUEWINE::REPOSITORY", url);
        savePersistentMap();
    }

    // ===========================================================================
    /**
     * Returns the list of source versions obtained by reading the
     * packages.idx file, located in the currently defined source repository.
     * If no packages.idx could be found, an empty list is returned.
     *
     * @return The list of version.
     * @throws IOException If an error occurs.
     */
    public List<SourceVersion> getSourceVersions() throws IOException
    {
        List<SourceVersion> versions = new ArrayList<SourceVersion>();

        URL url = new URL(getSourceRepositoryURL() + "packages.idx");
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            while (reader.ready())
            {
                String s = reader.readLine();
                if (s != null && s.trim().length() > 0)
                {
                    String[] split = s.split(";");

                    CodeSource cs = sourcesMap.get("/" + split[0]);
                    if (cs != null)
                    {
                        SourceVersion version = null;
                        if (split.length == 3) version = new SourceVersion(cs, split[1], split[2]);
                        else if (split.length == 2) version = new SourceVersion(cs, split[1], split[1]);
                        versions.add(version);
                    }
                }
            }
        }
        finally
        {
            if (reader != null) reader.close();
        }

        return versions;
    }

    // ===========================================================================
    /**
     * Returns the list of codesources that can be updated.
     *
     * @return The list of sources to update.
     * @throws IOException If an error occurs.
     */
    public List<SourceVersion> getCodeSourceToUpdate() throws IOException
    {
        List<SourceVersion> latest = getSourceVersions();
        List<SourceVersion> updates = new ArrayList<SourceVersion>();

        for (SourceVersion vers : latest)
        {
            if (!vers.getSource().getChecksum().equals(vers.getVersion()))
                updates.add(vers);
        }

        return updates;
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
            List<File> toActivate = new ArrayList<File>();
            for (String s : toadd)
            {
                URL url = new URL(s);
                String jar = url.getFile();
                File f = new File(root, jar);
                toActivate.add(fetch(url, f));
            }

            List<File> activated = new ArrayList<File>();
            for (File f : toActivate)
                activated.add(activate(f));

            List<CodeSource> sources = new ArrayList<CodeSource>();
            for (File f : activated)
                sources.add(getCodeSource(f, null));

            mapLoaders();

            for (CodeSourceListener l : listeners)
                l.codeSourceAdded(sources);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

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
            propertiesUsed.add(name);
        }
        finally
        {
            if (input != null) input.close();
        }

        return props;
    }

    // ===========================================================================
    /**
     * Returns the set of property files in use.
     *
     * @return The set of file names.
     */
    public Set<String> getPropertiesUsed()
    {
        Set<String> sorted = new TreeSet<String>();
        sorted.addAll(propertiesUsed);
        return sorted;
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
        l.addAll(sourcesMap.values());
        return l;
    }

    // ===========================================================================
    /**
     * Removes the given list of codesources. This does not do a physical remove,
     * but only a deregistration of the associated classloaders, and returns the
     * list of ALL codesources that have been removed.
     *
     * @param toRemove The sources to remove.
     * @return The list of CodeSources that were removed.
     */
    @SuppressWarnings("unchecked")
    public List<CodeSource> removeSources(List<CodeSource> toRemove)
    {
        for (CodeSourceListener l : listeners)
            l.codeSourceRemoved(toRemove);

        for (CodeSource src : toRemove)
        {
            sourcesMap.remove(src.getDisplayName());
            if (src instanceof JarCodeSource)
            {
                JarCodeSource jcs = (JarCodeSource) src;
                jcs.closeLoader();
                if (!jcs.getFile().delete())
                    System.out.println("Could not delete " + jcs.getFile().getAbsolutePath());
            }

            else if (src instanceof URLCodeSource)
            {
                URLCodeSource ucs = (URLCodeSource) src;
                // We cannot delete the url, so we'll have to register it
                // with the set of 'removed' urls:
                HashSet<String> removedUrls = (HashSet<String>) persistentMap.get("GLUEWINE:REMOVEDURLS");
                if (removedUrls == null)
                {
                    removedUrls = new HashSet<String>();
                    persistentMap.put("GLUEWINE:REMOVEDURLS", removedUrls);
                }
                ucs.closeLoader();
                removedUrls.add(ucs.getURL().toExternalForm());
                savePersistentMap();
            }

            else if (src instanceof DirectoryCodeSource)
            {
                DirectoryCodeSource dcs = (DirectoryCodeSource) src;
                List<CodeSource> children = codeSources.remove(dcs.getDirectory().getAbsolutePath());
                removeSources(children);
                dcs.closeLoader();
                dcs.getDirectory().delete();
            }
        }

        return toRemove;
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
        List<CodeSource> sourcesToRemove = new ArrayList<CodeSource>();

        for (String s : toRemove)
        {
            List<String> displayNames = new ArrayList<String>();
            displayNames.addAll(sourcesMap.keySet());
            for (String disp : displayNames)
            {
                if (disp.startsWith(s))
                    sourcesToRemove.add(sourcesMap.get(disp));
            }
        }
        return removeSources(sourcesToRemove);
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

            boolean initStdIn = false;

            if (clazz.equals("console")) clazz = "org.gluewine.console.impl.ConsoleClient";
            if (args.length > 1 && args[1].equals("gwt")) initStdIn = true;

            Launcher fw = getInstance();
            CodeSource rootCs = fw.sourcesMap.get(fw.getShortName(fw.root));
            Class<?> cl = rootCs.getSourceClassLoader().loadClass(clazz);
            String[] params = new String[args.length - 1];
            if (args.length > 1) System.arraycopy(args, 1, params, 0, params.length);

            cl.getMethod("main", String[].class).invoke(null, new Object[] {params});

            if (initStdIn) new Thread(fw).start();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    // ===========================================================================
    @Override
    public void run()
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true)
        {
            try
            {
                String line = in.readLine();
                if ("shutdown".equals(line)) break;
            }
            catch (Throwable e)
            {
            }
        }
        System.exit(0);
    }

    // ===========================================================================
    /**
     * Adds a listener.
     *
     * @param l The listener to add.
     */
    public void addListener(CodeSourceListener l)
    {
        listeners.add(l);
    }

    // ===========================================================================
    /**
     * Removes a listener.
     *
     * @param l The listener to remove.
     */
    public void removeListener(CodeSourceListener l)
    {
        listeners.remove(l);
    }
}
