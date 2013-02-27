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
 * Defines a listener that will be notified every time an object has either been registered or unregistered and that is
 * an instance of the generic type specified.
 *
 * @author fks/Serge de Schaetzen
 *
 * @param <T> The generic class.
 */
public interface RepositoryListener<T extends Object>
{
    // ===========================================================================
    /**
     * Invoked when an instance of the given type has been registered.
     *
     * @param t Tje class that was registered.
     */
    void registered(T t);

    // ===========================================================================
    /**
     * Invoked when an instance of the given type has been unregistered.
     *
     * @param t The class that was unregistered.
     */
    void unregistered(T t);
}
