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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.gluewine.launcher.utils.SHA1Utils;

public class JarCodeSource extends AbstractCodeSource
{
    // ===========================================================================
    /**
     * The file embedded.
     */
    private File file = null;

    // ===========================================================================
    /**
     * Creates an instance with the given file.
     *
     * @param file The file.
     * @throws MalformedURLException
     */
    public JarCodeSource(File file) throws MalformedURLException
    {
        super("Local Jar", new URL[] {file.toURI().toURL()});
        this.file = file;
        indexJarFile();
    }

    // ===========================================================================
    /**
     * Returns the associated file.
     *
     * @return The file.
     */
    public File getFile()
    {
        return file;
    }

    // ===========================================================================
    /**
     * Indexes the jar file.
     */
    private void indexJarFile()
    {
        JarFile jar = null;
        try
        {
            jar = new JarFile(file);
            Manifest manifest = jar.getManifest();
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

            if (getChecksum().equals("n/a"))
                setChecksum(SHA1Utils.getSHA1HashCode(file));

            if (getVersion().equals("n/a"))
            {
                String name = file.getName();
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
            if (jar != null)
            {
                try
                {
                    jar.close();
                }
                catch (Throwable e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
