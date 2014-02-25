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

    /**
     * The url.
     */
    private String url = null;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param source The source.
     * @param checksum The checksum of the new file.
     * @param version The new version checksum.
     * @param url The url.
     */
    public SourceVersion(CodeSource source, String checksum, String version, String url)
    {
        this.source = source;
        this.checksum = checksum;
        this.version = version;
        this.url = url;
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
     * Returns the url.
     *
     * @return The url.
     */
    public String getUrl()
    {
        return url;
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
