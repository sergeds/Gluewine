/**************************************************************************
 *
 * Gluewine REST JSon Module
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
package org.gluewine.rest_json;

import java.io.IOException;

import org.gluewine.rest.AbstractRESTSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serializes/Deserializes objects using JSON.
 *
 * @author fks/Serge de Schaetzen
 *
 */
public class JSONRESTSerializer extends AbstractRESTSerializer
{
    // ===========================================================================
    /**
     * The Jackson object mapper.
     */
    private ObjectMapper mapper = new ObjectMapper();

    // ===========================================================================
    /**
     * Creates an instance.
     */
    public JSONRESTSerializer()
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ===========================================================================
    @Override
    public String getFormat()
    {
        return "json";
    }

    // ===========================================================================
    @Override
    public String getResponseMIME()
    {
        return "application/json";
    }

    // ===========================================================================
    @Override
    public String serialize(Object o) throws IOException
    {
        return mapper.writeValueAsString(o);
    }

    // ===========================================================================
    @Override
    protected Object deserializeObject(Class<?> cl, String str) throws IOException
    {
        return mapper.readValue(str, cl);
    }
}
