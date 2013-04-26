/**************************************************************************
 *
 * Gluewine Database Authentication Module
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
package org.gluewine.dbauth;

import org.gluewine.authentication.AuthenticationException;
import org.gluewine.authentication.Authenticator;
import org.gluewine.authentication.UseridPasswordAuthentication;

/**
 * Defines the DBAuthenticator. This authenticator uses a userid and password
 * stored in a database.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface DBAuthenticator extends Authenticator, UseridPasswordAuthentication
{
    // ===========================================================================
    /**
     * Adds a credential for the given user and password.
     *
     * @param user The user to add.
     * @param password The password.
     * @throws AuthenticationException If an error occurred.
     */
    void addCredential(String user, String password) throws AuthenticationException;

    // ===========================================================================
    /**
     * Deletes the credential for the given user.
     *
     * @param user The user to add.
     * @throws AuthenticationException If an error occurred.
     */
    void delCredential(String user) throws AuthenticationException;

    // ===========================================================================
    /**
     * Resets the password of an existing user.
     *
     * @param user The user to reset.
     * @param password The new password.
     * @throws AuthenticationException If an error occurs.
     */
    void resetPassword(String user, String password) throws AuthenticationException;
}
