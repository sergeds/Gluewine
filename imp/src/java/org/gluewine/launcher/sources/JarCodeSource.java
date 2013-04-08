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

/**
 * Loads code from a jar/zip file.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class JarCodeSource extends URLCodeSource
{
    // ===========================================================================
    /**
     * The file used by the code source.
     */
    private File file = null;

    // ===========================================================================
    /**
     * Creates an instance with the given file.
     *
     * @param file The file.
     * @throws MalformedURLException If the file cannot be translated to a URL.
     */
    public JarCodeSource(File file) throws MalformedURLException
    {
        super("LocaL Jar", file.toURI().toURL());
        this.file = file;
    }

    // ===========================================================================
    /**
     * Returns the file used by this code source.
     *
     * @return The file.
     */
    public File getFile()
    {
        return file;
    }
}
