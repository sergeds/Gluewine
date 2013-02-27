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
 * Exception thrown by ServiceProviders when a request is made
 * to an non existing service.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class NoSuchServiceException extends Exception
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 3259281562689590715L;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param msg The message of the exception.
     */
    public NoSuchServiceException(String msg)
    {
        super(msg);
    }

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param msg The message of the exception.
     * @param e The cause of the exception.
     */
    public NoSuchServiceException(String msg, Throwable e)
    {
        super(msg, e);
    }
}
