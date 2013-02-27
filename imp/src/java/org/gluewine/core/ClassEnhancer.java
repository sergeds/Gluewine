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
 * Defines a class that can enhance other classes.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface ClassEnhancer
{
    // ===========================================================================
    /**
     * Returns an enhanced version of the given class, and instantiates it.
     *
     * @param <T> The generic class;
     * @param c The class to enhance;
     * @return The type.
     * @throws Throwable If an error occurs.
     */
    <T> T getEnhanced(Class<T> c) throws Throwable;
}
