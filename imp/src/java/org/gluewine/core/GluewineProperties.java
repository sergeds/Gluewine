/**************************************************************************
 *
 * Gluewine Core Module
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
package org.gluewine.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.gluewine.launcher.Launcher;


/**
 * Properties extension that accepts listeners to be registered.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class GluewineProperties extends Properties
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = -3769089923730257060L;

    /**
     * The set of registered listeners.
     */
    private Set<PropertyListener> listeners = new HashSet<PropertyListener>();

    /**
     * The properties file name relative to the gluewine.cfgdir.
     */
    private String name = null;

    /**
     * The map of property files.
     */
    private static Map<String, WeakReference<GluewineProperties>> properties = new HashMap<String, WeakReference<GluewineProperties>>();

    // ===========================================================================
    /**
     * Creates an instance using the name specified. The name is the name of the
     * file relative to the gluewine.cfgdir.
     *
     * @param name the name of the file.
     */
    public GluewineProperties(String name)
    {
        this.name = name;

        properties.put(name, new WeakReference<GluewineProperties>(this));
    }

    // ===========================================================================
    /**
     * Adds a listener.
     *
     * @param listener The listener to add.
     */
    public void addListener(PropertyListener listener)
    {
        synchronized (listeners)
        {
            listeners.add(listener);
        }
    }

    // ===========================================================================
    /**
     * Removes a listener.
     *
     * @param listener The listener to remove.
     */
    public void removeListener(PropertyListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(listener);
        }
    }

    // ===========================================================================
    /**
     * Notifies all registered listeners that the object has been refreshed.
     */
    public void notifyRefeshed()
    {
        synchronized (listeners)
        {
            for (PropertyListener l : listeners)
                l.propertiesChanged(this);
        }
    }

    // ===========================================================================
    /**
     * Request the property file to be loaded.
     *
     * @throws IOException Thrown if an error occurs reading the file.
     */
    public void load() throws IOException
    {
        File f = new File(Launcher.getInstance().getConfigDirectory(), name);
        InputStream input = null;
        try
        {
            input = new FileInputStream(f);
            load(input);
        }
        finally
        {
            if (input != null) input.close();
        }
    }

    // ===========================================================================
    @Override
    public void load(InputStream input) throws IOException
    {
        clear();
        super.load(input);
        notifyRefeshed();
    }

    // ===========================================================================
    @Override
    public void load(Reader reader) throws IOException
    {
        clear();
        super.load(reader);
        notifyRefeshed();
    }

    // ===========================================================================
    @Override
    public void loadFromXML(InputStream input) throws IOException
    {
        clear();
        super.loadFromXML(input);
        notifyRefeshed();
    }

    // ===========================================================================
    /**
     * Returns the active properties.
     *
     * @return The active properties.
     */
    public static Map<String, GluewineProperties> getActiveProperties()
    {
        Map<String, GluewineProperties> m = new HashMap<String, GluewineProperties>();

        for (Entry<String, WeakReference<GluewineProperties>> e : properties.entrySet())
        {
            GluewineProperties p = e.getValue().get();
            if (p != null) m.put(e.getKey(), p);
        }

        return m;
    }

    // ===========================================================================
    @Override
    public boolean equals(Object g)
    {
        return super.equals(g);
    }

    // ===========================================================================
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
}
