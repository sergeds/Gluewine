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


import jline.console.ConsoleReader;

import org.gluewine.authentication.AuthenticationAbortedException;

/**
 * Text console client authenticator.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class DBAuthenticatorTxtClient
{
    // ===========================================================================
    /**
     * Performs the authentication using the given server and reader.
     *
     * @param authenticator The server to authenticate against.
     * @param reader The reader to use for input.
     * @return The session id, if authentication was successfull.
     * @throws Throwable If an error occurs reading from the input.
     */
    public String authenticate(DBAuthenticator authenticator, ConsoleReader reader) throws Throwable
    {
        boolean authenticated = false;
        String id = null;
        while (!authenticated)
        {
            String user = reader.readLine("userid: ");
            String pw = reader.readLine("password: ", '\0');
            if (user.equals("close")) throw new AuthenticationAbortedException();
            try
            {
                id = authenticator.authenticate(user, pw);
                authenticated = true;
            }
            catch (Throwable e)
            {
                System.out.println("Invalid user or password!");
            }
        }
        return id;
    }
}
