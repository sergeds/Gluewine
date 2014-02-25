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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.gluewine.authentication.AuthenticationException;

/**
 * Entity used to store a credential (userid + password) in the database.
 *
 * @author fks/Serge de Schaetzen
 *
 */
@Entity
@Table(name = "sec_dbauth")
public class DBCredential
{
    /**
     * The user id.
     */
    @Id
    private String userid = null;

    /**
     * The encrypted password.
     */
    private byte[] digest = null;

    /**
     * The encryption salt.
     */
    private byte[] salt = null;

    /**
     * The length of the salt.
     */
    private static final int SALT_LENGTH = 8;

    // ===========================================================================
    /**
     * Sets the userid.
     *
     * @param id The userid.
     */
    public void setUserid(String id)
    {
        this.userid = id;
    }

    // ===========================================================================
    /**
     * Returns the userid.
     *
     * @return The userid.
     */
    public String getUserid()
    {
        return userid;
    }

    // ===========================================================================
    /**
     * Verifies that the given password matches the users encypted password, and returns
     * true if and only if they match.
     *
     * @param password The password to check.
     * @return True if they match.
     * @throws AuthenticationException Thrown if the password verification could not be done.
     */
    public boolean verify(String password) throws AuthenticationException
    {
        if (salt != null && digest != null)
        {
            byte[] enc = getHash(password);
            return Arrays.equals(enc, digest);
        }
        else
            return false;
    }

    // ===========================================================================
    /**
     * Sets the users password.
     *
     * @param password The users password.
     * @throws AuthenticationException Thrown if the salt generation or encryption failed.
     */
    public void setPassword(String password) throws AuthenticationException
    {
        try
        {
            SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
            salt = new byte[SALT_LENGTH];
            r.nextBytes(salt);
            digest = getHash(password);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new AuthenticationException("An error occurred encrypting the password !");
        }
    }

    // ===========================================================================
    /**
     * Creates the hash of the given password.
     *
     * @param pw The password to process.
     * @return The hash.
     * @throws AuthenticationException Thrown if the encryption failed.
     */
    private byte[] getHash(String pw) throws AuthenticationException
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(salt);
            return digest.digest(pw.getBytes("UTF-8"));
        }
        catch (Throwable e)
        {
            throw new AuthenticationException("Error changing password");
        }
    }
}
