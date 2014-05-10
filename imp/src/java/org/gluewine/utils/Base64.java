/**************************************************************************
 *
 * Gluewine Utility Module
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
package org.gluewine.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Utility class allowing to encode and decode base64 strings.
 *
 * @author Gluewine/Serge de Schaetzen
 *
 */
public final class Base64
{
    // ===========================================================================
    /** The allowed chars. */
    private static final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    /** Used for reverse mapping. */
    private static final int[] REVERSE_MAPPING = new int[123];

    /**
     * Initializes the reverse map.
     */
    static
    {
        for (int i = 0; i < CHARS.length; i++)
            REVERSE_MAPPING[CHARS[i]] = i + 1;
    }

    // ===========================================================================
    /**
     * Use the static methods to access the class.
     */
    private Base64()
    {
    }

    // ===========================================================================
    /**
     * Encodes the given byte array as a String64 String.
     *
     * @param input The bytes to encode.
     * @return The Base64 String.
     */
    public static String encode(byte[] input)
    {
        StringBuffer result = new StringBuffer();
        int outputCharCount = 0;
        for (int i = 0; i < input.length; i += 3)
        {
            int remaining = Math.min(3, input.length - i);
            int oneBigNumber = (input[i] & 0xff) << 16 | (remaining <= 1 ? 0 : input[i + 1] & 0xff) << 8 | (remaining <= 2 ? 0 : input[i + 2] & 0xff);
            for (int j = 0; j < 4; j++)
                result.append(remaining + 1 > j ? CHARS[0x3f & oneBigNumber >> 6 * (3 - j)] : '=');

            outputCharCount += 4;
            if (outputCharCount % 76 == 0)
                result.append('\n');
        }
        return result.toString();
    }

    // ===========================================================================
    /**
     * Decodes the given Base64 String and returns a byte[].
     *
     * @param input The Base64 String.
     * @return The decoded bytes.
     */
    public static byte[] decode(String input)
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StringReader in = new StringReader(input);
            for (int i = 0; i < input.length(); i += 4)
            {
                int[] a = {mapCharToInt(in), mapCharToInt(in), mapCharToInt(in), mapCharToInt(in)};
                int oneBigNumber = (a[0] & 0x3f) << 18 | (a[1] & 0x3f) << 12 | (a[2] & 0x3f) << 6 | (a[3] & 0x3f);
                for (int j = 0; j < 3; j++)
                    if (a[j + 1] >= 0)
                        out.write(0xff & oneBigNumber >> 8 * (2 - j));
            }
            return out.toByteArray();
        }
        catch (IOException e)
        {
            throw new Error(e + ": " + e.getMessage());
        }
    }

    // ===========================================================================
    /**
     * Maps the chars to int for the given reader.
     *
     * @param input The reader to process.
     * @return The int.
     * @throws IOException If a problem occurs while reading.
     */
    private static int mapCharToInt(Reader input) throws IOException
    {
        int c;
        while ((c = input.read()) != -1)
        {
            int result = REVERSE_MAPPING[c];
            if (result != 0)
                return result - 1;
            if (c == '=')
                return -1;
        }
        return -1;
    }
}
