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
import java.io.IOException;
import java.net.URL;

import org.gluewine.launcher.DirectoryAnnotations;
import org.gluewine.launcher.utils.FileUtils;

/**
 * CodeSource for a directory.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DirectoryCodeSource extends AbstractCodeSource implements DirectoryAnnotations
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
        File services = new File(directory, SERVICES);
        if (services.exists())
        {
            for (String service : FileUtils.readFile(services))
                addService(service);
        }

        File entities = new File(directory, ENTITIES);
        if (entities.exists())
        {
            for (String entity : FileUtils.readFile(entities))
                addEntity(entity);
        }

        File enhancers = new File(directory, ENHANCERS);
        if (enhancers.exists())
        {
            for (String enhancer : FileUtils.readFile(enhancers))
                addEnhancer(enhancer);
        }
    }
}
