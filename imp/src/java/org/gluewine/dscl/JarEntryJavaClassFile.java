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
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.tools.SimpleJavaFileObject;

/**
 * JavaClassFile represented by an URI.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class JarEntryJavaClassFile extends SimpleJavaFileObject
{
    // ===========================================================================
    /**
     * The URI of the entry.
     */
    private URI uri = null;

    /**
     * The class name.
     */
    private String className = null;

    /**
     * The zip file containing the class.
     */
    private File zip = null;

    /**
     * The name of the entry.
     */
    private String entry = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param uri The uri pointing to the class.
     * @param className The classname.
     * @param entry The entry name.
     * @param zip The zip file.
     */
    public JarEntryJavaClassFile(URI uri, String className, String entry, File zip)
    {
        super(uri, Kind.CLASS);
        this.uri = uri;
        this.className = className;
        this.entry = entry;
        this.zip = zip;
    }

    // ===========================================================================
    @Override
    @SuppressWarnings("resource")
    public InputStream openInputStream() throws IOException
    {
        ZipFile zf = new ZipFile(zip);
        ZipEntry ze = zf.getEntry(entry);
        return zf.getInputStream(ze);
    }

    // ===========================================================================
    /**
     * Returns the class name.
     *
     * @return The class name.
     */
    public String getClassName()
    {
        return className;
    }

    // ===========================================================================
    /**
     * Returns the uri of this file.
     *
     * @return The URI.
     */
    public URI getURI()
    {
        return uri;
    }
}
