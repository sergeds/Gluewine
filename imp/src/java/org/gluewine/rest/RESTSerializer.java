package org.gluewine.rest;

import java.io.IOException;

/**
 * Defines a class that can be used to serialize/deserialize data to/from Strings.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public interface RESTSerializer
{
    // ===========================================================================
    /**
     * Returns the format name this class can handle.
     *
     * @return The format name.
     */
    String getFormat();

    // ===========================================================================
    /**
     * Returns the MIM Type of the response.
     *
     * @return The MIME type.
     */
    String getResponseMIME();

    // ===========================================================================
    /**
     * Deserializes an object from the given string and of the given class,
     * and returns it.
     *
     * @param cl The class to be deserialized.
     * @param str The string to deserialize.
     * @return The object.
     * @throws IOException If an error occurs.
     */
    Object deserialize(Class<?> cl, String[] str) throws IOException;

    // ===========================================================================
    /**
     * Serializes the object specified to a String.
     *
     * @param o The object to serialize.
     * @return The String representation of the object.
     * @throws IOException If an error occurs.
     */
    String serialize(Object o) throws IOException;
}
