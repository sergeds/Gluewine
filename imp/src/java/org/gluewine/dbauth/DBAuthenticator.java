/**************************************************************************
 *
 * Gluewine Database Authentication Module
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
