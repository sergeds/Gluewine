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

/**
 * Defines the framework repository service.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface Repository
{
    // ===========================================================================
    /**
     * Registers the given object with the framework.
     *
     * @param o The object to register.
     */
    void register(Object o);

    // ===========================================================================
    /**
     * Removes the given object from the framework.
     * @param o The object to unregister.
     */
    void unregister(Object o);

    // ===========================================================================
    /**
     * Adds a listener.
     *
     * @param listener The listener to add.
     */
    void addListener(RepositoryListener<?> listener);

    // ===========================================================================
    /**
     * Removes a listener.
     *
     * @param listener The listener to remove.
     */
    void removeListener(RepositoryListener<?> listener);

    // ===========================================================================
    /**
     * Returns an instance of the given type, or null if no such instance is available.
     *
     * @param <T> The generic type of the service.
     * @param c The type of the instance to return.
     * @return The (possibly null) instance.
     */
    <T> T getService(Class<T> c);
}
