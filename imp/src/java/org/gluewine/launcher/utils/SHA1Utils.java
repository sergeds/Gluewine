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
package org.gluewine.launcher.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class that offers some SHA1 computations.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class SHA1Utils
{
    // ===========================================================================
    /**
     * Use the static methods to access the class.
     */
    private SHA1Utils()
    {
    }

    // ===========================================================================
    /**
     * Computes the SHA1 hashcode of the file.
     *
     * @param input The String to process.
     * @return The hashcode.
     * @throws NoSuchAlgorithmException If the SHA1 algorithm is not defined.
     */
    public static String getSHA1HashCode(String input) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] dataBytes = input.getBytes();
        md.update(dataBytes, 0, dataBytes.length);
        return hashCodeToString(md.digest());
    }

    // ===========================================================================
    /**
     * Computes the SHA1 hashcode of the file.
     *
     * @param f The file to process.
     * @return The hashcode.
     * @throws IOException If a problem occurs opening the file.
     */
    public static String getSHA1HashCode(File f) throws IOException
    {
        FileInputStream fis = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            fis = new FileInputStream(f);
            byte[] dataBytes = new byte[1024];

            int nread = 0;

            while ((nread = fis.read(dataBytes)) != -1)
                md.update(dataBytes, 0, nread);

            return hashCodeToString(md.digest());
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            throw new IOException(e.getMessage());
        }
        finally
        {
            if (fis != null)
                fis.close();
        }
    }

    // ===========================================================================
    /**
     * Converts the given bytes to a hex String.
     *
     * @param hash The bytes.
     * @return The String.
     */
    private static String hashCodeToString(byte[] hash)
    {
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < hash.length; i++)
            sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));

        return sb.toString();
    }
}
