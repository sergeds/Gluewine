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
 * Defines a class that can provide proxies to remote services.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface ServiceProvider
{
    // ===========================================================================
    /**
     * Requests to obtain a proxy to a service of the given class.
     *
     * @param cl The class to proxy.
     * @return The proxy.
     * @throws NoSuchServiceException If no such service is available.
     */
    Object getService(Class<?> cl) throws NoSuchServiceException;
}
