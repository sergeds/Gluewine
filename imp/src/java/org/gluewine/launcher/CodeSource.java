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
    GluewineLoader getSourceClassLoader();

    // ===========================================================================
    /**
     * If true, SQL files should be parsed.
     *
     * @return True of SQL files are to be parsed.
     */
    boolean loadSQL();

    // ===========================================================================
    /**
     * Sets the classloader.
     *
     * @param loader The classloader.
     */
    void setSourceClassLoader(GluewineLoader loader);

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

    // ===========================================================================
    /**
     * Returns true if the source has changed. Ie, the one loaded is outdated
     * from the one stored in the source.
     *
     * @return True if changed.
     */
    boolean hasChanged();
}
