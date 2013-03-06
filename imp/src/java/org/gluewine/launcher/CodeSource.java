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

import java.net.URL;

/**
 * Defines a source of code.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface CodeSource
{
    // ===========================================================================
    /**
     * Returns the name to be displayed.
     *
     * @return The display name.
     */
    String getDisplayName();

    // ===========================================================================
    /**
     * Sets the display name.
     *
     * @param name The display name.
     */
    void setDisplayName(String name);

    // ===========================================================================
    /**
     * Returns the urls used by thos code source.
     *
     * @return The urls.
     */
    URL[] getURLs();

    // ===========================================================================
    /**
     * Returns the list of services defined in this source. The services are
     * defined as class names.
     *
     * @return The list of services.
     */
    String[] getServices();

    // ===========================================================================
    /**
     * Returns the list of entities defined in this source. The entities are
     * defined as class names.
     *
     * @return The list of entities.
     */
    String[] getEntities();

    // ===========================================================================
    /**
     * Returns the list of enhancers defined in this source. The enhancers are
     * defined as class names.
     *
     * @return The list of enhancers.
     */
    String[] getEnhancers();

    // ===========================================================================
    /**
     * Returns the classloader to use to load classes from this source.
     *
     * @return The classloader.
     */
    ClassLoader getSourceClassLoader();

    // ===========================================================================
    /**
     * Sets the classloader.
     *
     * @param loader The classloader.
     */
    void setSourceClassLoader(ClassLoader loader);

    // ===========================================================================
    /**
     * Returns the type of source. (informational)
     *
     * @return The type.
     */
    String getType();

    // ===========================================================================
    /**
     * Closes the classloader.
     */
    void closeLoader();

    // ===========================================================================
    /**
     * Returns the build date.
     *
     * @return The build date.
     */
    String getBuildDate();

    // ===========================================================================
    /**
     * Returns the revision.
     *
     * @return The revision.
     */
    String getRevision();

    // ===========================================================================
    /**
     * Returns the repository revision.
     *
     * @return The repository revision.
     */
    String getReposRevision();

    // ===========================================================================
    /**
     * Returns the build number.
     *
     * @return The build number.
     */
    String getBuildNumber();

    // ===========================================================================
    /**
     * Returns the SHA1 checksum.
     *
     * @return The checksum.
     */
    String getChecksum();

    // ===========================================================================
    /**
     * Returns the version.
     *
     * @return The version.
     */
    String getVersion();
}
