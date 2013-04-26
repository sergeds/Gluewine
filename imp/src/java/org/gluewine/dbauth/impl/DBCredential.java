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
package org.gluewine.dbauth.impl;

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
    void setUserid(String id)
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
    boolean verify(String password) throws AuthenticationException
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
