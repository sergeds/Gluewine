/**************************************************************************
 *
 * Gluewine Authentication Module
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
package org.gluewine.authentication;

/**
 * Defines authentication using userid and password.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface UseridPasswordAuthentication
{
    // ===========================================================================
    /**
     * Authenticates using the given userid and password.
     *
     * @param user The user to authenticate.
     * @param password The password of the user.
     * @return The session id.
     * @throws AuthenticationException If authentication failed.
     */
    String authenticate(String user, String password) throws AuthenticationException;
    
    //test
}
