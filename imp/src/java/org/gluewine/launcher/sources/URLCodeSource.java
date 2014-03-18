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
package org.gluewine.launcher.sources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.gluewine.launcher.utils.FileUtils;

/**
 * Loads code from a remote jar/zip file.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class URLCodeSource extends AbstractCodeSource
{
    // ===========================================================================
    /**
     * The URL.
     */
    private URL url = null;

    // ===========================================================================
    /**
     * Creates an instance with the given file.
     *
     * @param url The url.
     * @throws MalformedURLException If the file cannot be translated to a URL.
     */
    public URLCodeSource(URL url) throws MalformedURLException
    {
        this("Remote JAR", url);
    }

    // ===========================================================================
    /**
     * Creates an instance with the given file.
     *
     * @param name The name of the file.
     * @param url The url.
     * @throws MalformedURLException If the file cannot be translated to a URL.
     */
    protected URLCodeSource(String name, URL url) throws MalformedURLException
    {
        super(name, new URL[] {url});
        this.url = url;
        indexJarFile(url);
    }

    // ===========================================================================
    /**
     * Returns the url.
     *
     * @return The url.
     */
    public URL getURL()
    {
        return url;
    }

    // ===========================================================================
    /**
     * Indexes the jar file.
     *
     * @param url The url pointing to the file.
     */
    protected void indexJarFile(URL url)
    {
        JarInputStream jin  = null;
        try
        {
            jin = new JarInputStream(getURLs()[0].openStream());
            //jar = new JarFile(file);
            Manifest manifest = jin.getManifest();
            if (manifest != null)
            {
                Attributes attr = manifest.getMainAttributes();
                String act = attr.getValue("Gluewine-Services");
                if (act != null)
                {
                    act = act.trim();
                    String[] cl = act.split(",");
                    for (String c : cl)
                        addService(c.trim());
                }

                String ent = attr.getValue("Gluewine-Entities");
                if (ent != null)
                {
                    ent = ent.trim();
                    String[] cl = ent.split(",");
                    for (String c : cl)
                        addEntity(c.trim());
                }

                String sql = attr.getValue("Gluewine-Sql");
                if (sql != null)
                    setLoadSQL("true".equalsIgnoreCase(sql));

                String enh = attr.getValue("Gluewine-Enhancer");
                if (enh != null)
                {
                    enh = enh.trim();
                    String[] cl = enh.split(",");
                    for (String c : cl)
                        addEnhancer(c.trim());
                }

                String fks = attr.getValue("X-Fks-BuildDate");
                if (fks != null)
                    setBuildDate(fks.trim());

                fks = attr.getValue("X-Fks-Revision");
                if (fks != null)
                    setRevision(fks.trim());

                fks = attr.getValue("X-Fks-RepoRevision");
                if (fks != null)
                    setReposRevision(fks.trim());

                fks = attr.getValue("X-Fks-BuildNumber");
                if (fks != null)
                    setBuildNumber(fks.trim());

                fks = attr.getValue("X-Fks-Checksum");
                if (fks != null)
                    setChecksum(fks.trim());

                fks = attr.getValue("Jar-Version");
                if (fks != null)
                    setVersion(fks.trim());
            }

            if (getChecksum().length() == 0)
                setChecksum(FileUtils.getSHA1HashCode(getURLs()[0]));

            if (getVersion().length() == 0)
            {
                String name = getURLs()[0].getFile();
                int i = name.lastIndexOf('-');
                if (i > 0)
                {
                    int j = name.lastIndexOf('.');
                    if (j > i)
                        setVersion(name.substring(i + 1, j));
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (jin != null)
            {
                try
                {
                    jin.close();
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    // ===========================================================================
    @Override
    public boolean hasChanged()
    {
        JarInputStream jin  = null;
        String currCheckSum = null;
        try
        {

            jin = new JarInputStream(getURLs()[0].openStream());
            //jar = new JarFile(file);
            Manifest manifest = jin.getManifest();
            if (manifest != null)
            {
                Attributes attr = manifest.getMainAttributes();
                String fks = attr.getValue("X-Fks-Checksum");
                if (fks != null)
                    currCheckSum = fks.trim();

            }

            if (currCheckSum == null)
                currCheckSum = FileUtils.getSHA1HashCode(getURLs()[0]);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

        finally
        {
            if (jin != null)
            {
                try
                {
                    jin.close();
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }

        return !getChecksum().equals(currCheckSum);
    }
}
