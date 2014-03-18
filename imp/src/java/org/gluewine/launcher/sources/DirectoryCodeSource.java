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
     * Returns the directory this codesource points at.
     *
     * @return The directory.
     */
    public File getDirectory()
    {
        return directory;
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

    // ===========================================================================
    @Override
    public boolean hasChanged()
    {
        return false;
    }
}
