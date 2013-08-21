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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.gluewine.launcher.CodeSource;
import org.gluewine.launcher.GluewineLoader;

/**
 * Abstract implementation of CodeSource. It keeps track
 * of the common properties.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public abstract class AbstractCodeSource implements CodeSource
{
    // ===========================================================================
    /**
     * The classloader used.
     */
    private GluewineLoader classLoader = null;

    /**
     * The display name.
     */
    private String displayName = null;

    /**
     * The list of enhancers.
     */
    private List<String> enhancers = new ArrayList<String>();

    /**
     * The list of entities.
     */
    private List<String> entities = new ArrayList<String>();

    /**
     * The list of services.
     */
    private List<String> services = new ArrayList<String>();

    /**
     * The type of source.
     */
    private String type = null;

    /**
     * The urls used.
     */
    private URL[] urls = null;

    /**
     * The build date.
     */
    private String buildDate = "";

    /**
     * The revision.
     */
    private String revision = "";

    /**
     * The repos revision.
     */
    private String reposRevision = "";

    /**
     * The buildnumber.
     */
    private String buildNumber = "";

    /**
     * The checksum.
     */
    private String checksum = "";

    /**
     * The version of the source.
     */
    private String version = "";

    /**
     * Flag indicating whether SQL files should be processed or not.
     */
    private boolean processSql = false;

    // ===========================================================================
    /**
     * Creates an instance with the given urls.
     *
     * @param type The type of codesource.
     * @param urls The urls to use.
     */
    public AbstractCodeSource(String type, URL[] urls)
    {
        this.type = type;
        this.urls = urls;
    }

    // ===========================================================================
    /**
     * Adds an enhancer.
     *
     * @param enhancer The enhancer to add.
     */
    protected void addEnhancer(String enhancer)
    {
        enhancers.add(enhancer);
    }

    // ===========================================================================
    /**
     * Adds an entity.
     *
     * @param entity The entity to add.
     */
    protected void addEntity(String entity)
    {
        entities.add(entity);
    }

    // ===========================================================================
    /**
     * Adds a service.
     *
     * @param service The service to add.
     */
    protected void addService(String service)
    {
        services.add(service);
    }

    // ===========================================================================
    @Override
    public String getDisplayName()
    {
        return displayName;
    }

    // ===========================================================================
    @Override
    public String[] getEnhancers()
    {
        return enhancers.toArray(new String[enhancers.size()]);
    }

    // ===========================================================================
    @Override
    public String[] getEntities()
    {
        return entities.toArray(new String[entities.size()]);
    }

    // ===========================================================================
    @Override
    public String[] getServices()
    {
        return services.toArray(new String[services.size()]);
    }

    // ===========================================================================
    @Override
    public GluewineLoader getSourceClassLoader()
    {
        return classLoader;
    }

    // ===========================================================================
    @Override
    public String getType()
    {
        return type;
    }

    // ===========================================================================
    @Override
    public URL[] getURLs()
    {
        URL[] u = new URL[urls.length];
        System.arraycopy(urls, 0, u, 0, u.length);
        return u;
    }

    // ===========================================================================
    @Override
    public void setDisplayName(String name)
    {
        this.displayName = name;
    }

    // ===========================================================================
    @Override
    public void setSourceClassLoader(GluewineLoader loader)
    {
        this.classLoader = loader;
    }

    // ===========================================================================
    @Override
    public void closeLoader()
    {
        try
        {
            classLoader.close();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    // ===========================================================================
    @Override
    public String getBuildDate()
    {
        return buildDate;
    }

    // ===========================================================================
    /**
     * Sets the build date.
     *
     * @param date The date.
     */
    protected void setBuildDate(String date)
    {
        this.buildDate = date;
    }

    // ===========================================================================
    @Override
    public String getRevision()
    {
        return revision;
    }

    // ===========================================================================
    /**
     * Sets the revision.
     *
     * @param revision The revision.
     */
    protected void setRevision(String revision)
    {
        this.revision = revision;
    }

    // ===========================================================================
    @Override
    public String getReposRevision()
    {
        return reposRevision;
    }

    // ===========================================================================
    /**
     * Sets the repos revision.
     *
     * @param revision The repos revision.
     */
    protected void setReposRevision(String revision)
    {
        this.reposRevision = revision;
    }

    // ===========================================================================
    @Override
    public String getBuildNumber()
    {
        return buildNumber;
    }

    // ===========================================================================
    /**
     * Sets the build number.
     *
     * @param number The build number.
     */
    protected void setBuildNumber(String number)
    {
        this.buildNumber = number;
    }

    // ===========================================================================
    @Override
    public String getChecksum()
    {
        return checksum;
    }

    // ===========================================================================
    /**
     * Sets the checksum.
     *
     * @param check The checksum.
     */
    protected void setChecksum(String check)
    {
        this.checksum = check;
    }

    // ===========================================================================
    @Override
    public String getVersion()
    {
        return version;
    }

    // ===========================================================================
    /**
     * Sets the version.
     *
     * @param version The version.
     */
    protected void setVersion(String version)
    {
        this.version = version;
    }

    // ===========================================================================
    @Override
    public boolean loadSQL()
    {
        return processSql;
    }

    // ===========================================================================
    /**
     * Sets the process SQL flag.
     *
     * @param processSql The SQL flag.
     */
    protected void setLoadSQL(boolean processSql)
    {
        this.processSql = processSql;
    }
}
