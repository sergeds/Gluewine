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
 * This interface defines annotations that can be added to directories.
 * <br>These are of course no real java annotations, but files with a
 * specific meaning.
 *
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface DirectoryAnnotations
{
    // ===========================================================================
    /**
     * The annotation indicating that only one loader should be used for a directory.
     * (instead of one per jar).
     */
    String SINGLELOADER = "@SingleLoader";

    /**
     * Annotation that defines a file containing URLs to be loaded.
     * (1 per line).
     */
    String URLLOADER = "@UrlLoader";

    /**
     * Annotation that defines a file containing a list of services to be instantiated.
     * (1 per line).
     */
    String SERVICES = "@Services";

    /**
     * Annotation that defines a file containing Hibernate entities to be registered.
     * (1 per line)
     */
    String ENTITIES = "@Entities";

    /**
     * Annotation that defines a file containing Enhancers to be instantiated.
     */
    String ENHANCERS = "@Enhancers";

    /**
     * Annotation that defines a file containing a list of URLs (1 per line) of jar/zip files
     * to retrieve and install in the directory.
     */
    String INSTALL = "@Install";
}
