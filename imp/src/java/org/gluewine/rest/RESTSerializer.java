/**************************************************************************
 *
 * Gluewine REST Module
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
