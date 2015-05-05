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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * This code source is to be used in an Eclipse environment (for interactive debugging) and allows to load services and entities from manifest files before they are packaged in the jar files. These manifest files must have a name that ends with
 * '.manifest'.
 *
 * It will not load classes nor resources, (this is handled by eclipse).
 *
 * @author Gluewine/Serge de Schaetzen
 *
 */
public class SourceDirCodeSource extends AbstractCodeSource
{
    // ===========================================================================
    /**
     * Creates an instance. The directory given points to the root of the source directory.
     *
     * @param dir The source directory.
     * @throws IOException Thrown if an error occurs reading the manifest file.
     */
    public SourceDirCodeSource(String dir) throws IOException
    {
        super("Source", new URL[0]);
        File f = new File(dir);
        setDisplayName(dir);
        if (f.exists() && f.isDirectory())
            process(f);
        else
            throw new IOException("Invalid source directory " + dir);
    }

    // ===========================================================================
    /**
     * Processes the given directory. This is done recursively.
     *
     * @param dir The directory to process.
     * @throws IOException Thrown if an error occurs reading the manifest file.
     */
    private void process(File dir) throws IOException
    {
        File[] files = dir.listFiles();
        for (File file : files)
        {
            if (file.isFile() && file.getName().endsWith(".manifest"))
                loadManifest(file);

            else if (file.isDirectory())
                process(file);
        }
    }

    // ===========================================================================
    /**
     * Loads a manifest file.
     * @param file the file to load
     * @throws IOException Thrown if an error occurs reading the manifest file.
     */
    private void loadManifest(File file) throws IOException
    {
        InputStream input = null;
        try
        {
            input = new FileInputStream(file);
            Manifest m = new Manifest(input);
            Attributes attr = m.getMainAttributes();
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
        }
        finally
        {
            if (input != null)
                input.close();
        }
    }

    // ===========================================================================
    @Override
    public boolean hasChanged()
    {
        return false;
    }

}
