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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeSource for a directory.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DirectoryCodeSource extends AbstractCodeSource
{
    // ===========================================================================
    /**
     * The directory used.
     */
    private File directory = null;

    // ===========================================================================
    /**
     * Creates an instance for the directory specified.
     *
     * @param dir The directory.
     * @throws IOException Thrown if a read error occurs.
     */
    public DirectoryCodeSource(File dir) throws IOException
    {
        super("Directory", new URL[] {dir.toURI().toURL()});
        this.directory = dir;
        initialize();
    }

    // ===========================================================================
    /**
     * Initializes the directory by checking the presence of a:
     * <br>services.lst file and
     * <br>entities.lst.
     *
     * @throws IOException Thrown if a read error occurs.
     */
    private void initialize() throws IOException
    {
        File services = new File(directory, "services.lst");
        if (services.exists())
        {
            for (String service : readFile(services))
                addService(service);
        }

        File entities = new File(directory, "entities.lst");
        if (entities.exists())
        {
            for (String entity : readFile(entities))
                addEntity(entity);
        }

        File enhancers = new File(directory, "enhancers.lst");
        if (enhancers.exists())
        {
            for (String enhancer : readFile(enhancers))
                addEnhancer(enhancer);
        }
    }

    // ===========================================================================
    /**
     * Reads the file specified and returns its content as a list of Strings.
     * Empty lines and lines starting with # are removed.
     *
     * @param file The file to process.
     * @return The content.
     * @throws IOException Thrown if a read error occurs.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DM_DEFAULT_ENCODING")
    private List<String> readFile(File file) throws IOException
    {
        BufferedReader reader = null;
        List<String> content = new ArrayList<String>();
        try
        {
            reader = new BufferedReader(new FileReader(file));
            while (reader.ready())
            {
                String line = reader.readLine().trim();
                if (!line.startsWith("#"))
                    content.add(line);
            }
        }
        finally
        {
            if (reader != null) reader.close();
        }

        return content;
    }
}
