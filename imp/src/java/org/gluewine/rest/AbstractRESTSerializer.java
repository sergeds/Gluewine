package org.gluewine.rest;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * Abstract implementation of RESTSerializer. It offers serialization/deserialization methods
 * for primitives and Strings.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public abstract class AbstractRESTSerializer implements RESTSerializer
{
    // ===========================================================================
    /**
     * URL decodes the given array of Strings.
     *
     * @param s The strings to decode.
     * @return The decodec Strings.
     * @throws IOException If the decoder is not supported.
     */
    protected String[] decode(String[] s) throws IOException
    {
        String[] dec = new String[s.length];
        for (int i = 0; i < s.length; i++)
            dec[i] = URLDecoder.decode(s[i], "utf-8");
        return dec;
    }

    // ===========================================================================
    @Override
    public Object deserialize(Class<?> cl, String[] str) throws IOException
    {

        String[] decoded = decode(str);
        Object res = null;
        if (cl.isArray()) res = toArray(cl, decoded);
        else res = toObject(cl, decoded[0]);
        return res;
    }

    // ===========================================================================
    /**
     * Requests the serializer instance to deserialize the given String to an object of
     * the requested class.
     *
     * @param cl The resulting class.
     * @param str The string to deserialize.
     * @return The object.
     * @throws IOException If the object could not be deserialized.
     */
    protected abstract Object deserializeObject(Class<?> cl, String str) throws IOException;

    // ===========================================================================
    /**
     * Converts the string specified to an object.
     *
     * @param cl The resulting class.
     * @param value The value to convert.
     * @return The resulting object.
     * @throws IOException If an error occurs or the value could not be converted.
     */
    protected Object toObject(Class<?> cl, String value) throws IOException
    {
        Object res = null;
        switch (cl.getName())
        {
            case "boolean" :
                res = Boolean.parseBoolean(value);
                break;

            case "byte" :
                res = Byte.parseByte(value);
                break;

            case "char" :
                res = value.toCharArray()[0];
                break;

            case "double" :
                res = Double.parseDouble(value);
                break;

            case "float" :
                res = Float.parseFloat(value);
                break;

            case "long" :
                res = Long.parseLong(value);
                break;

            case "int" :
                res = Integer.parseInt(value);
                break;

            case "short" :
                res = Short.parseShort(value);
                break;

            case "java.lang.String" :
                res = value;
                break;

            default :
                res = deserializeObject(cl, value);
        }

        return res;
    }

    // ===========================================================================
    /**
     * Converts the given values to an array.
     *
     * @param cl The resulting class.
     * @param values The values to convert.
     * @return The resulting object.
     * @throws IOException If an object could not be deserialized.
     */
    protected Object toArray(Class<?> cl, String[] values) throws IOException
    {
        Object[] res = new Object[values.length];
        Class<?> comp = cl.getComponentType();

        for (int i = 0; i < values.length; i++)
            res[i] = toObject(comp, values[i]);

        return res;
    }
}
