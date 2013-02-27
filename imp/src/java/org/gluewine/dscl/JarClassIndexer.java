/**************************************************************************
 *
 * Gluewine DSCL Enhancer Module
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
package org.gluewine.dscl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.tools.JavaFileObject;

import org.gluewine.launcher.Launcher;


/**
 * Indexes all the classes from the jar files in the lib directory.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class JarClassIndexer
{
    // ===========================================================================
    /**
     * The map of FileObjects per package.
     */
    private Map<String, List<JavaFileObject>> filesPerPackage = new HashMap<String, List<JavaFileObject>>();

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @throws IOException If an error occurs reading a jar file.
     * @throws URISyntaxException If an error occurs indexing a jar file.
     */
    JarClassIndexer() throws IOException, URISyntaxException
    {
        ZipFile zip = null;

        try
        {
            for (File file : Launcher.getInstance().getJarFiles())
            {
                StringBuilder b = new StringBuilder("jar://");
                String filename = file.getAbsolutePath().replace('\\', '/');
                if (!filename.startsWith("/")) b.append("/");
                b.append(filename).append("!/");
                String baseUri = b.toString();

                zip = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements())
                {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".class"))
                    {
                        String pack = getPackageName(name);
                        String uri = baseUri + name;

                        List<JavaFileObject> files = filesPerPackage.get(pack);
                        if (files == null)
                        {
                            files = new ArrayList<JavaFileObject>();
                            filesPerPackage.put(pack, files);
                        }

                        int i = name.indexOf(".class");
                        files.add(new JarEntryJavaClassFile(new URI(uri), name.substring(0, i).replace('/', '.'), name, file));
                    }
                }
            }
        }
        finally
        {
            if (zip != null) zip.close();
        }
    }

    // ===========================================================================
    /**
     * Returns the list of JavaFileObjects for the given package name.
     *
     * @param packageName The package name to process.
     * @return The list of file objects.
     */
    public List<JavaFileObject> getListOfJavaFileObjects(String packageName)
    {
        return filesPerPackage.get(packageName);
    }

    // ===========================================================================
    /**
     * Returns the package name of the specified entry.
     *
     * @param entry The entry to process.
     * @return The package name.
     */
    private String getPackageName(String entry)
    {
        int i = entry.lastIndexOf("/");
        if (i > 0)
            entry = entry.substring(0, i);

        return entry.replace('/', '.');
    }
}
