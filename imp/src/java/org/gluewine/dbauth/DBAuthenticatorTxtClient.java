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

import jline.ConsoleReader;

import org.gluewine.console.AuthenticationAbortedException;

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
