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
package org.gluewine.launcher;

/**
 * Defines the version (checksum based) of a source stored
 * in a remote location.
 *
 * The versions are read from a package.idx file.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class SourceVersion
{
    // ===========================================================================
    /**
     * The checksum of the file.
     */
    private String checksum = null;

    /**
     * The version checksum.
     */
    private String version = null;

    /**
     * The local code source.
     */
    private CodeSource source = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param source The source.
     * @param checksum The checksum of the new file.
     * @param version The new version checksum.
     */
    public SourceVersion(CodeSource source, String checksum, String version)
    {
        this.source = source;
        this.checksum = checksum;
        this.version = version;
    }

    // ===========================================================================
    /**
     * Returns the source.
     *
     * @return The source.
     */
    public CodeSource getSource()
    {
        return source;
    }

    // ===========================================================================
    /**
     * Returns the file checksum.
     *
     * @return The checksum.
     */
    public String getChecksum()
    {
        return checksum;
    }

    // ===========================================================================
    /**
     * Returns the version checksum.
     *
     * @return The version checksum.
     */
    public String getVersion()
    {
        return version;
    }
}
