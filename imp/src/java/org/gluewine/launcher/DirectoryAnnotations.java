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
