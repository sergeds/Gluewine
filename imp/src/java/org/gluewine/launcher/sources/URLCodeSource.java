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
}
