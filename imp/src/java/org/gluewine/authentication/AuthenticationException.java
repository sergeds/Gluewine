/**************************************************************************
 *
 * Gluewine Authentication Module
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
package org.gluewine.authentication;

/**
 * An exception that is thrown when an authentication fails, or
 * when a call is performed without being authenticated.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class AuthenticationException extends Exception
{
    // ===========================================================================
    /**
     * The serial uid.
     */
    private static final long serialVersionUID = 5857448827600693095L;

    // ===========================================================================
    /**
     * Creates an instance.
     *
     * @param msg The message.
     */
    public AuthenticationException(String msg)
    {
        super(msg);
    }
}
