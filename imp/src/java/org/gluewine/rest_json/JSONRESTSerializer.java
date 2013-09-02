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
