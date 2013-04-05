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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that offers some SHA1 computations.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public final class FileUtils
{
    // ===========================================================================
    /**
     * Use the static methods to access the class.
     */
    private FileUtils()
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
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DM_DEFAULT_ENCODING")
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
        return getSHA1HashCode(f.toURI().toURL());
    }

    // ===========================================================================
    /**
     * Computes the SHA1 hashcode of the file.
     *
     * @param url The url to process.
     * @return The hashcode.
     * @throws IOException If a problem occurs opening the file.
     */
    public static String getSHA1HashCode(URL url) throws IOException
    {
        InputStream is = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            is = url.openStream();
            byte[] dataBytes = new byte[1024];

            int nread = 0;

            while ((nread = is.read(dataBytes)) != -1)
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
            if (is != null)
                is.close();
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

    // ===========================================================================
    /**
     * Reads the file specified and returns its content as a list of Strings.
     * Empty lines and lines starting with # are removed.
     *
     * @param file The file to process.
     * @return The content.
     * @throws IOException Thrown if a read error occurs.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DM_DEFAULT_ENCODING")
    public static List<String> readFile(File file) throws IOException
    {
        BufferedReader reader = null;
        List<String> content = new ArrayList<String>();
        try
        {
            reader = new BufferedReader(new FileReader(file));
            while (reader.ready())
            {
                String line = reader.readLine().trim();
                if (!line.startsWith("#"))
                    content.add(line);
            }
        }
        finally
        {
            if (reader != null) reader.close();
        }

        return content;
    }
}
